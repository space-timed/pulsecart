package com.pulsecart.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.notification.domain.NotificationChannel;
import com.pulsecart.notification.dto.NotificationDtos.*;
import com.pulsecart.notification.listener.NotificationEventListener;
import com.pulsecart.notification.repository.DlqMessageRepository;
import com.pulsecart.notification.repository.NotificationRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationEventListener eventListener;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DlqMessageRepository dlqMessageRepository;

    @Test
    @DisplayName("Should send notification and render template variables accurately")
    void testSendNotificationDirectly() throws Exception {
        SendNotificationRequest request = new SendNotificationRequest(
                "alice@example.com",
                NotificationChannel.EMAIL,
                "ORDER_CONFIRMED",
                "Your Order is Confirmed",
                Map.of(
                        "customerName", "Alice Smith",
                        "orderId", "ord-101",
                        "totalAmount", "149.99"
                ),
                "ord-101"
        );

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipient").value("alice@example.com"))
                .andExpect(jsonPath("$.channel").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.content", containsString("Alice Smith")))
                .andExpect(jsonPath("$.content", containsString("ord-101")))
                .andExpect(jsonPath("$.content", containsString("149.99")));

        // Query by recipient
        mockMvc.perform(get("/api/v1/notifications?recipient=alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].orderId").value("ord-101"));
    }

    @Test
    @DisplayName("Should process OrderConfirmed event from Kafka topic and create notification")
    void testKafkaOrderEventProcessing() {
        String eventPayload = """
                {
                  "eventId": "evt-ord-999",
                  "eventType": "OrderConfirmed",
                  "timestamp": "2026-03-29T12:00:00Z",
                  "payload": {
                    "orderId": "ord-kafka-999",
                    "customerId": "cust-kafka-77",
                    "totalAmount": "299.00"
                  }
                }
                """;

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                PulseCartTopics.ORDER_EVENTS,
                0,
                10L,
                "ord-kafka-999",
                eventPayload
        );

        eventListener.onOrderEvent(record);

        var notifications = notificationRepository.findByOrderId("ord-kafka-999");
        assertFalse(notifications.isEmpty(), "Notification should be generated for Kafka OrderConfirmed event");
        assertEquals("cust-kafka-77@pulsecart.io", notifications.get(0).getRecipient());
        assertTrue(notifications.get(0).getContent().contains("299.00"));
    }

    @Test
    @DisplayName("Should catch poisoned/corrupted payload, route to DLQ messages table, and allow replay")
    void testPoisonPillDlqRoutingAndReplay() throws Exception {
        String poisonPill = "{ INVALID_JSON_DATA_POISON_PILL: true, broken }";

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                PulseCartTopics.ORDER_EVENTS,
                1,
                88L,
                "poison-key",
                poisonPill
        );

        eventListener.onOrderEvent(record);

        // Verify DLQ messages endpoint lists the unhandled poison pill
        mockMvc.perform(get("/api/v1/notifications/dlq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].topic").value(PulseCartTopics.ORDER_EVENTS))
                .andExpect(jsonPath("$[0].replayed").value(false));

        var pendingDlq = dlqMessageRepository.findByReplayedFalseOrderByFailedAtDesc();
        assertFalse(pendingDlq.isEmpty());
        Long dlqId = pendingDlq.get(0).getId();

        // Replay DLQ message
        mockMvc.perform(post("/api/v1/notifications/dlq/" + dlqId + "/replay"))
                .andExpect(status().isOk());

        var replayedMsg = dlqMessageRepository.findById(dlqId).orElseThrow();
        assertTrue(replayedMsg.isReplayed(), "DLQ message must be marked as replayed");
    }
}
