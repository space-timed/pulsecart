package com.pulsecart.shipping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.shipping.domain.ShippingCarrier;
import com.pulsecart.shipping.domain.ShippingOutboxEvent;
import com.pulsecart.shipping.dto.ShippingDtos.*;
import com.pulsecart.shipping.repository.ShippingOutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShippingServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShippingOutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("Should dispatch shipment via FedEx adapter, generate tracking number, and write outbox event")
    void testFedExShipmentDispatch() throws Exception {
        DispatchShipmentRequest request = new DispatchShipmentRequest(
                "ord-ship-100",
                "cust-ship-100",
                ShippingCarrier.FEDEX,
                "500 Fast Track Blvd, Memphis, TN"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/shipments/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ord-ship-100"))
                .andExpect(jsonPath("$.carrier").value("FEDEX"))
                .andExpect(jsonPath("$.trackingNumber", startsWith("FDX-")))
                .andExpect(jsonPath("$.status").value("DISPATCHED"))
                .andExpect(jsonPath("$.estimatedDeliveryAt", notNullValue()))
                .andReturn();

        ShipmentResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ShipmentResponse.class);

        // Verify lookup by tracking number
        mockMvc.perform(get("/api/v1/shipments/tracking/" + response.trackingNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()));

        // Verify Outbox Event
        List<ShippingOutboxEvent> outboxList = outboxEventRepository.findAll();
        assertFalse(outboxList.isEmpty(), "Outbox events must contain ShippingDispatched");
        ShippingOutboxEvent event = outboxList.stream()
                .filter(e -> e.getAggregateId().equals(response.id()))
                .findFirst()
                .orElseThrow();
        assertEquals("ShippingDispatched", event.getEventType());
    }

    @Test
    @DisplayName("Should dispatch shipment via UPS adapter with UPS tracking format")
    void testUpsShipmentDispatch() throws Exception {
        DispatchShipmentRequest request = new DispatchShipmentRequest(
                "ord-ups-200",
                "cust-ups-200",
                ShippingCarrier.UPS,
                "600 Brown Box Ave, Atlanta, GA"
        );

        mockMvc.perform(post("/api/v1/shipments/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carrier").value("UPS"))
                .andExpect(jsonPath("$.trackingNumber", startsWith("1Z-")));
    }

    @Test
    @DisplayName("Should guarantee idempotent shipping dispatch for duplicate order ID")
    void testIdempotentShippingDispatch() throws Exception {
        DispatchShipmentRequest request = new DispatchShipmentRequest(
                "ord-idem-ship-300",
                "cust-idem-300",
                ShippingCarrier.DHL,
                "700 International Way"
        );

        // First call
        MvcResult firstResult = mockMvc.perform(post("/api/v1/shipments/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ShipmentResponse first = objectMapper.readValue(firstResult.getResponse().getContentAsString(), ShipmentResponse.class);

        // Second call
        MvcResult secondResult = mockMvc.perform(post("/api/v1/shipments/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        ShipmentResponse second = objectMapper.readValue(secondResult.getResponse().getContentAsString(), ShipmentResponse.class);

        assertEquals(first.id(), second.id(), "Idempotent shipment must return identical shipment ID");
        assertEquals(first.trackingNumber(), second.trackingNumber(), "Tracking number must match exactly");
    }
}
