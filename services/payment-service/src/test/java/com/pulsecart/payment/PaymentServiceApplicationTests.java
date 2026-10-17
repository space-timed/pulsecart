package com.pulsecart.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.payment.domain.PaymentProvider;
import com.pulsecart.payment.domain.PaymentStatus;
import com.pulsecart.payment.dto.PaymentDtos.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should authorize, capture, and refund payment via Stripe provider adapter")
    void testPaymentLifecycle() throws Exception {
        // 1. Authorize Payment
        AuthorizePaymentRequest authReq = new AuthorizePaymentRequest(
                "ord-pay-100",
                "cust-pay-100",
                new BigDecimal("149.99"),
                "USD",
                PaymentProvider.STRIPE,
                "tok_visa_4242",
                null
        );

        MvcResult authResult = mockMvc.perform(post("/api/v1/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.amount").value(149.99))
                .andExpect(jsonPath("$.authorizationCode", notNullValue()))
                .andReturn();

        AuthorizePaymentResponse authResp = objectMapper.readValue(
                authResult.getResponse().getContentAsString(), AuthorizePaymentResponse.class);

        // 2. Capture Payment
        mockMvc.perform(post("/api/v1/payments/" + authResp.paymentId() + "/capture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.captureReference", notNullValue()));

        // 3. Refund Payment
        RefundPaymentRequest refundReq = new RefundPaymentRequest(new BigDecimal("149.99"), "Customer requested refund");
        mockMvc.perform(post("/api/v1/payments/" + authResp.paymentId() + "/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refundReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.refundedAmount").value(149.99));
    }

    @Test
    @DisplayName("Should guarantee idempotency when duplicate Idempotency-Key is received")
    void testIdempotentPaymentAuthorization() throws Exception {
        String idempotencyKey = "idem-test-uuid-999888";

        AuthorizePaymentRequest req = new AuthorizePaymentRequest(
                "ord-idem-200",
                "cust-idem-200",
                new BigDecimal("89.50"),
                "USD",
                PaymentProvider.STRIPE,
                "tok_mastercard",
                null
        );

        // First call
        MvcResult firstResult = mockMvc.perform(post("/api/v1/payments/authorize")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andReturn();

        AuthorizePaymentResponse firstResp = objectMapper.readValue(
                firstResult.getResponse().getContentAsString(), AuthorizePaymentResponse.class);

        // Second duplicate call with same Idempotency-Key
        MvcResult secondResult = mockMvc.perform(post("/api/v1/payments/authorize")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andReturn();

        AuthorizePaymentResponse secondResp = objectMapper.readValue(
                secondResult.getResponse().getContentAsString(), AuthorizePaymentResponse.class);

        // Both calls must return identical paymentId and authorizationCode
        assertEquals(firstResp.paymentId(), secondResp.paymentId(), "Idempotent payment must return identical paymentId");
        assertEquals(firstResp.authorizationCode(), secondResp.authorizationCode(), "Idempotent authorization code must match");
    }

    @Test
    @DisplayName("Should inject deterministic chaos failures for Failure Laboratory")
    void testDeterministicChaosInjection() throws Exception {
        AuthorizePaymentRequest chaosReq = new AuthorizePaymentRequest(
                "ord-chaos-300",
                "cust-chaos-300",
                new BigDecimal("500.00"),
                "USD",
                PaymentProvider.CHAOS_MOCK,
                "tok_chaos_fail",
                "FAIL_INSUFFICIENT_FUNDS"
        );

        mockMvc.perform(post("/api/v1/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chaosReq)))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errorMessage", containsString("Insufficient balance")));
    }
}
