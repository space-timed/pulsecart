package com.pulsecart.fulfillment.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.fulfillment.dto.FulfillmentDtos.FulfillmentItemRequest;
import com.pulsecart.fulfillment.dto.FulfillmentDtos.ProcessFulfillmentRequest;
import com.pulsecart.fulfillment.service.FulfillmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final FulfillmentService fulfillmentService;
    private final ObjectMapper objectMapper;

    public OrderEventListener(FulfillmentService fulfillmentService, ObjectMapper objectMapper) {
        this.fulfillmentService = fulfillmentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = PulseCartTopics.ORDER_EVENTS, groupId = "pulsecart-fulfillment-group")
    public void onOrderEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "";

            if ("order.confirmed".equalsIgnoreCase(eventType) || "OrderConfirmed".equalsIgnoreCase(eventType)) {
                JsonNode payload = root.get("payload");
                if (payload != null) {
                    String orderId = payload.has("orderId") ? payload.get("orderId").asText() : "";
                    String customerId = payload.has("customerId") ? payload.get("customerId").asText() : "";
                    String shippingAddress = payload.has("shippingAddress") ? payload.get("shippingAddress").asText() : "";

                    List<FulfillmentItemRequest> items = new ArrayList<>();
                    if (payload.has("items") && payload.get("items").isArray()) {
                        for (JsonNode itemNode : payload.get("items")) {
                            items.add(new FulfillmentItemRequest(
                                    itemNode.has("productId") ? itemNode.get("productId").asText() : "",
                                    itemNode.has("sku") ? itemNode.get("sku").asText() : "",
                                    itemNode.has("productName") ? itemNode.get("productName").asText() : "",
                                    itemNode.has("quantity") ? itemNode.get("quantity").asInt() : 1
                            ));
                        }
                    }

                    if (!orderId.isBlank()) {
                        log.info("Fulfillment service received order.confirmed event for order: {}", orderId);
                        ProcessFulfillmentRequest request = new ProcessFulfillmentRequest(orderId, customerId, shippingAddress, items);
                        fulfillmentService.processFulfillment(request);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error processing Kafka order event in fulfillment-service: {}", e.getMessage());
        }
    }
}
