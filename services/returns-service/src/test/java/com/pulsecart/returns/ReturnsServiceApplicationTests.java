package com.pulsecart.returns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.returns.domain.ReturnStatus;
import com.pulsecart.returns.domain.ReturnsOutboxEvent;
import com.pulsecart.returns.dto.ReturnsDtos.*;
import com.pulsecart.returns.repository.ReturnsOutboxEventRepository;
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
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReturnsServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReturnsOutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("Should approve eligible return within 30-day window, issue RMA, and record outbox event")
    void testEligibleReturnRequestGeneratesRmaAndOutboxEvent() throws Exception {
        CreateReturnRequest request = new CreateReturnRequest(
                "ord-ret-001",
                "cust-ret-001",
                Instant.now().minus(Duration.ofDays(7)), // delivered 7 days ago
                "Wrong size received",
                List.of(
                        new ReturnItemRequest("prod-shirt-01", 2, "UNOPENED", new BigDecimal("35.00")),
                        new ReturnItemRequest("prod-socks-02", 1, "UNOPENED", new BigDecimal("12.50"))
                )
        );

        MvcResult result = mockMvc.perform(post("/api/v1/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", startsWith("RMA-")))
                .andExpect(jsonPath("$.orderId").value("ord-ret-001"))
                .andExpect(jsonPath("$.customerId").value("cust-ret-001"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.refundAmount").value(82.50))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andReturn();

        ReturnResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ReturnResponse.class);

        // Verify retrieval by RMA ID
        mockMvc.perform(get("/api/v1/returns/" + response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.refundAmount").value(82.50));

        // Verify outbox event written
        List<ReturnsOutboxEvent> outboxList = outboxEventRepository.findAll();
        assertFalse(outboxList.isEmpty());
        ReturnsOutboxEvent event = outboxList.stream()
                .filter(e -> e.getAggregateId().equals(response.id()))
                .findFirst()
                .orElseThrow();
        assertEquals("ReturnApproved", event.getEventType());
    }

    @Test
    @DisplayName("Should reject return request exceeding 30-day return window policy")
    void testIneligibleReturnPast30DaysRejection() throws Exception {
        CreateReturnRequest request = new CreateReturnRequest(
                "ord-ret-expired-002",
                "cust-ret-002",
                Instant.now().minus(Duration.ofDays(45)), // delivered 45 days ago (> 30 days)
                "Changed mind after a month and a half",
                List.of(new ReturnItemRequest("prod-watch-09", 1, "OPENED", new BigDecimal("199.99")))
        );

        MvcResult result = mockMvc.perform(post("/api/v1/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", startsWith("RMA-")))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reason", containsString("Exceeded 30-day return window")))
                .andReturn();

        ReturnResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ReturnResponse.class);

        // Verify outbox event for rejection
        List<ReturnsOutboxEvent> outboxList = outboxEventRepository.findAll();
        ReturnsOutboxEvent event = outboxList.stream()
                .filter(e -> e.getAggregateId().equals(response.id()))
                .findFirst()
                .orElseThrow();
        assertEquals("ReturnRejected", event.getEventType());
    }

    @Test
    @DisplayName("Should transition received goods to RECEIVED status and record ReturnReceived outbox event")
    void testReceiveReturnTransitionsToReceivedAndEmitsOutboxEvent() throws Exception {
        // Step 1: Create approved return
        CreateReturnRequest createReq = new CreateReturnRequest(
                "ord-ret-recv-003",
                "cust-ret-003",
                Instant.now().minus(Duration.ofDays(3)),
                "Defective item",
                List.of(new ReturnItemRequest("prod-speaker-55", 1, "DEFECTIVE", new BigDecimal("79.00")))
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        ReturnResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), ReturnResponse.class);

        // Step 2: Mark goods received at warehouse
        ProcessReturnReceiptRequest receiveReq = new ProcessReturnReceiptRequest("RESTOCKABLE", "Inspected by QC tech #4");

        mockMvc.perform(post("/api/v1/returns/" + created.id() + "/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()))
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        // Step 3: Verify ReturnReceived outbox event
        List<ReturnsOutboxEvent> outboxList = outboxEventRepository.findAll();
        boolean receivedEventExists = outboxList.stream()
                .anyMatch(e -> e.getAggregateId().equals(created.id()) && "ReturnReceived".equals(e.getEventType()));
        assertTrue(receivedEventExists, "ReturnReceived outbox event must be emitted");
    }
}
