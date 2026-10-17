package com.pulsecart.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.order.client.InventoryClient;
import com.pulsecart.order.client.InventoryClient.ReservationResult;
import com.pulsecart.order.client.PaymentClient;
import com.pulsecart.order.client.PaymentClient.PaymentAuthResult;
import com.pulsecart.order.domain.OrderStatus;
import com.pulsecart.order.domain.OutboxEvent;
import com.pulsecart.order.domain.SagaStatus;
import com.pulsecart.order.dto.OrderDtos.*;
import com.pulsecart.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private InventoryClient inventoryClient;

    @MockitoBean
    private PaymentClient paymentClient;

    @Test
    @DisplayName("Should successfully execute happy-path Saga: reserve stock, authorize payment, confirm order, write outbox")
    void testHappyPathCheckoutSaga() throws Exception {
        // Mock downstream dependencies
        Mockito.when(inventoryClient.reserveStock(anyString(), anyList()))
                .thenReturn(new ReservationResult(true, "res_order_1001", null));
        Mockito.when(inventoryClient.commitStock("res_order_1001"))
                .thenReturn(true);

        Mockito.when(paymentClient.authorizePayment(anyString(), anyString(), any(BigDecimal.class), any(), any()))
                .thenReturn(new PaymentAuthResult(true, "pay_order_1001", "AUTH_HAPPY_99", null));

        CheckoutRequest request = new CheckoutRequest(
                "cust-saga-100",
                List.of(
                        new CheckoutItemRequest("prod-1", "SKU-1", "Mechanical Keyboard", new BigDecimal("120.00"), 1),
                        new CheckoutItemRequest("prod-2", "SKU-2", "Gaming Mouse", new BigDecimal("50.00"), 2)
                ),
                "123 Distributed Way, Silicon Valley, CA",
                "tok_card_valid",
                null,
                null
        );

        MvcResult result = mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.sagaStatus").value("SUCCEEDED"))
                .andExpect(jsonPath("$.totalAmount").value(220.00))
                .andExpect(jsonPath("$.reservationId").value("res_order_1001"))
                .andExpect(jsonPath("$.paymentId").value("pay_order_1001"))
                .andReturn();

        CheckoutResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), CheckoutResponse.class);

        // Verify Transactional Outbox Event was created in the database
        List<OutboxEvent> outboxList = outboxEventRepository.findAll();
        assertFalse(outboxList.isEmpty(), "Outbox events must contain OrderConfirmed event");
        OutboxEvent event = outboxList.stream()
                .filter(e -> e.getAggregateId().equals(response.orderId()))
                .findFirst()
                .orElseThrow();
        assertEquals("OrderConfirmed", event.getEventType());
        assertEquals("pulsecart.order.events", event.getTopic());
        assertFalse(event.isPublished());
    }

    @Test
    @DisplayName("Should execute Saga Compensation when payment fails: release reserved stock and cancel order")
    void testPaymentFailureSagaCompensation() throws Exception {
        // Mock inventory reservation success, but payment failure
        Mockito.when(inventoryClient.reserveStock(anyString(), anyList()))
                .thenReturn(new ReservationResult(true, "res_order_comp_2002", null));
        Mockito.when(inventoryClient.releaseStock("res_order_comp_2002"))
                .thenReturn(true);

        Mockito.when(paymentClient.authorizePayment(anyString(), anyString(), any(BigDecimal.class), any(), eq("FAIL_INSUFFICIENT_FUNDS")))
                .thenReturn(new PaymentAuthResult(false, null, null, "Simulated decline: Insufficient funds"));

        CheckoutRequest request = new CheckoutRequest(
                "cust-comp-200",
                List.of(new CheckoutItemRequest("prod-laptop", "SKU-LP", "MacBook Pro", new BigDecimal("2499.00"), 1)),
                "456 Chaos Lane",
                "tok_card_declined",
                null,
                "FAIL_INSUFFICIENT_FUNDS"
        );

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.sagaStatus").value("FAILED"))
                .andExpect(jsonPath("$.cancellationReason", containsString("PAYMENT_FAILED")));

        // Verify compensation action: releaseStock was invoked with the exact reservationId
        Mockito.verify(inventoryClient, Mockito.times(1)).releaseStock("res_order_comp_2002");
    }

    @Test
    @DisplayName("Should guarantee checkout idempotency with duplicate Idempotency-Key header")
    void testCheckoutIdempotency() throws Exception {
        Mockito.when(inventoryClient.reserveStock(anyString(), anyList()))
                .thenReturn(new ReservationResult(true, "res_idem_3003", null));
        Mockito.when(inventoryClient.commitStock("res_idem_3003"))
                .thenReturn(true);
        Mockito.when(paymentClient.authorizePayment(anyString(), anyString(), any(BigDecimal.class), any(), any()))
                .thenReturn(new PaymentAuthResult(true, "pay_idem_3003", "AUTH_IDEM_11", null));

        String idempotencyKey = "checkout-idem-unique-key-4455";

        CheckoutRequest request = new CheckoutRequest(
                "cust-idem-300",
                List.of(new CheckoutItemRequest("prod-screen", "SKU-SC", "4K Monitor", new BigDecimal("450.00"), 1)),
                "789 Idempotent Blvd",
                "tok_card_valid",
                null,
                null
        );

        // 1. First checkout
        MvcResult firstResult = mockMvc.perform(post("/api/v1/checkout")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn();

        CheckoutResponse firstResp = objectMapper.readValue(firstResult.getResponse().getContentAsString(), CheckoutResponse.class);

        // 2. Second checkout with identical Idempotency-Key
        MvcResult secondResult = mockMvc.perform(post("/api/v1/checkout")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn();

        CheckoutResponse secondResp = objectMapper.readValue(secondResult.getResponse().getContentAsString(), CheckoutResponse.class);

        // Both calls must return identical orderId and total
        assertEquals(firstResp.orderId(), secondResp.orderId(), "Idempotent checkout must return identical orderId");
        assertEquals(firstResp.totalAmount(), secondResp.totalAmount(), "Idempotent total must match");

        // Verify downstream was called only ONCE
        Mockito.verify(inventoryClient, Mockito.times(1)).reserveStock(anyString(), anyList());
        Mockito.verify(paymentClient, Mockito.times(1)).authorizePayment(anyString(), anyString(), any(BigDecimal.class), any(), any());
    }
}
