package com.pulsecart.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.analytics.dto.AnalyticsDtos.*;
import com.pulsecart.analytics.listener.AnalyticsEventListener;
import com.pulsecart.contracts.event.PulseCartTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalyticsEventListener eventListener;

    @Test
    @DisplayName("Should aggregate order sales into hourly revenue and product velocity metrics")
    void testRecordOrderSaleDirectly() throws Exception {
        RecordOrderSaleRequest request = new RecordOrderSaleRequest(
                "ord-an-101",
                "cust-an-101",
                new BigDecimal("339.97"),
                List.of(
                        new ItemSaleDto("prod-kb", "Pulse Mechanical Keyboard", 2, new BigDecimal("129.99")),
                        new ItemSaleDto("prod-mouse", "Pulse Wireless Gaming Mouse", 1, new BigDecimal("79.99"))
                )
        );

        mockMvc.perform(post("/api/v1/analytics/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Verify overview
        mockMvc.perform(get("/api/v1/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalRevenue", greaterThanOrEqualTo(339.97)))
                .andExpect(jsonPath("$.totalItemsSold", greaterThanOrEqualTo(3)));

        // Verify product velocity ranking
        mockMvc.perform(get("/api/v1/analytics/velocity?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].productId").value("prod-kb"))
                .andExpect(jsonPath("$[0].unitsSold").value(2))
                .andExpect(jsonPath("$[0].velocityScore").value(3.00));
    }

    @Test
    @DisplayName("Should process OrderConfirmed Kafka events and update analytics in real-time")
    void testOrderConfirmedKafkaEventStreaming() throws Exception {
        String eventJson = """
                {
                  "eventId": "evt-an-kafka-888",
                  "eventType": "OrderConfirmed",
                  "timestamp": "2026-03-29T14:30:00Z",
                  "payload": {
                    "orderId": "ord-an-kafka-888",
                    "customerId": "cust-kafka-99",
                    "totalAmount": "199.99",
                    "items": [
                      {
                        "productId": "prod-headset",
                        "productName": "Pulse Pro Noise-Cancelling Headset",
                        "quantity": 1,
                        "price": 199.99
                      }
                    ]
                  }
                }
                """;

        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                PulseCartTopics.ORDER_EVENTS, 0, 100L, "ord-an-kafka-888", eventJson
        );

        eventListener.onOrderEvent(record);

        // Verify analytics overview captured event
        mockMvc.perform(get("/api/v1/analytics/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalRevenue", greaterThanOrEqualTo(199.99)));
    }
}
