package com.pulsecart.fulfillment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.fulfillment.domain.FulfillmentOutboxEvent;
import com.pulsecart.fulfillment.domain.FulfillmentStatus;
import com.pulsecart.fulfillment.dto.FulfillmentDtos.*;
import com.pulsecart.fulfillment.repository.FulfillmentOutboxEventRepository;
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
class FulfillmentServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FulfillmentOutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("Should process fulfillment, assign warehouse, save items, and emit outbox event")
    void testFulfillmentLifecycle() throws Exception {
        ProcessFulfillmentRequest request = new ProcessFulfillmentRequest(
                "ord-ful-test-101",
                "cust-ful-101",
                "100 Warehouse Way, Seattle, WA",
                List.of(
                        new FulfillmentItemRequest("prod-1", "SKU-1", "Mechanical Keyboard", 1),
                        new FulfillmentItemRequest("prod-2", "SKU-2", "Gaming Mouse", 2)
                )
        );

        MvcResult result = mockMvc.perform(post("/api/v1/fulfillments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ord-ful-test-101"))
                .andExpect(jsonPath("$.status").value("FULFILLED"))
                .andExpect(jsonPath("$.warehouseId", startsWith("WH-")))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andReturn();

        FulfillmentResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), FulfillmentResponse.class);

        // Verify query endpoint
        mockMvc.perform(get("/api/v1/fulfillments/" + response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("ord-ful-test-101"));

        // Verify Outbox Event
        List<FulfillmentOutboxEvent> outboxList = outboxEventRepository.findAll();
        assertFalse(outboxList.isEmpty(), "Fulfillment outbox must contain event");
        FulfillmentOutboxEvent outboxEvent = outboxList.stream()
                .filter(e -> e.getAggregateId().equals(response.id()))
                .findFirst()
                .orElseThrow();
        assertEquals("FulfillmentCompleted", outboxEvent.getEventType());
    }

    @Test
    @DisplayName("Should guarantee idempotent fulfillment processing for duplicate order ID")
    void testIdempotentFulfillment() throws Exception {
        ProcessFulfillmentRequest request = new ProcessFulfillmentRequest(
                "ord-ful-idem-202",
                "cust-ful-202",
                "200 Idempotent Rd",
                List.of(new FulfillmentItemRequest("prod-screen", "SKU-SC", "Monitor", 1))
        );

        // First call
        MvcResult firstResult = mockMvc.perform(post("/api/v1/fulfillments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        FulfillmentResponse first = objectMapper.readValue(firstResult.getResponse().getContentAsString(), FulfillmentResponse.class);

        // Second call
        MvcResult secondResult = mockMvc.perform(post("/api/v1/fulfillments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        FulfillmentResponse second = objectMapper.readValue(secondResult.getResponse().getContentAsString(), FulfillmentResponse.class);

        assertEquals(first.id(), second.id(), "Idempotent fulfillment must return identical fulfillment ID");
        assertEquals(first.warehouseId(), second.warehouseId(), "Assigned warehouse must match");
    }
}
