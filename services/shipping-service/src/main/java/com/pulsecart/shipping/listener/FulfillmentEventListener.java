package com.pulsecart.shipping.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.shipping.domain.ShippingCarrier;
import com.pulsecart.shipping.dto.ShippingDtos.DispatchShipmentRequest;
import com.pulsecart.shipping.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FulfillmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentEventListener.class);

    private final ShippingService shippingService;
    private final ObjectMapper objectMapper;

    public FulfillmentEventListener(ShippingService shippingService, ObjectMapper objectMapper) {
        this.shippingService = shippingService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = PulseCartTopics.FULFILLMENT_EVENTS, groupId = "pulsecart-shipping-group")
    public void onFulfillmentEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "";

            if ("fulfillment.completed".equalsIgnoreCase(eventType) || "FulfillmentCompleted".equalsIgnoreCase(eventType)) {
                JsonNode payload = root.get("payload");
                if (payload != null) {
                    String orderId = payload.has("orderId") ? payload.get("orderId").asText() : "";
                    String customerId = payload.has("customerId") ? payload.get("customerId").asText() : "";

                    if (!orderId.isBlank()) {
                        log.info("Shipping service received fulfillment.completed event for order: {}", orderId);
                        DispatchShipmentRequest request = new DispatchShipmentRequest(
                                orderId, customerId, ShippingCarrier.FEDEX, "100 Customer Home, Austin, TX"
                        );
                        shippingService.dispatchShipment(request);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error processing Kafka fulfillment event in shipping-service: {}", e.getMessage());
        }
    }
}
