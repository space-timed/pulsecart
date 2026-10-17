package com.pulsecart.analytics.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.analytics.dto.AnalyticsDtos.ItemSaleDto;
import com.pulsecart.analytics.dto.AnalyticsDtos.RecordOrderSaleRequest;
import com.pulsecart.analytics.service.AnalyticsService;
import com.pulsecart.contracts.event.PulseCartTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class AnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventListener.class);

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public AnalyticsEventListener(AnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = PulseCartTopics.ORDER_EVENTS, groupId = "pulsecart-analytics-group")
    public void onOrderEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "";
            JsonNode payload = root.get("payload");

            if (payload == null) return;

            if ("OrderConfirmed".equalsIgnoreCase(eventType) || "order.confirmed".equalsIgnoreCase(eventType)) {
                String orderId = payload.has("orderId") ? payload.get("orderId").asText() : "";
                String customerId = payload.has("customerId") ? payload.get("customerId").asText() : "";
                BigDecimal totalAmount = payload.has("totalAmount") ? new BigDecimal(payload.get("totalAmount").asText()) : BigDecimal.ZERO;

                List<ItemSaleDto> items = new ArrayList<>();
                if (payload.has("items") && payload.get("items").isArray()) {
                    for (JsonNode itemNode : payload.get("items")) {
                        String prodId = itemNode.has("productId") ? itemNode.get("productId").asText() : "unknown-prod";
                        String prodName = itemNode.has("productName") ? itemNode.get("productName").asText() : prodId;
                        int qty = itemNode.has("quantity") ? itemNode.get("quantity").asInt() : 1;
                        BigDecimal price = itemNode.has("price") ? new BigDecimal(itemNode.get("price").asText()) : BigDecimal.ZERO;
                        items.add(new ItemSaleDto(prodId, prodName, qty, price));
                    }
                }

                RecordOrderSaleRequest request = new RecordOrderSaleRequest(orderId, customerId, totalAmount, items);
                analyticsService.recordOrderSale(request);
            }
        } catch (Exception e) {
            log.warn("Error processing order event in analytics listener: {}", e.getMessage());
        }
    }
}
