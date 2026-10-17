package com.pulsecart.notification.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.notification.domain.NotificationChannel;
import com.pulsecart.notification.dto.NotificationDtos.SendNotificationRequest;
import com.pulsecart.notification.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = PulseCartTopics.ORDER_EVENTS, groupId = "pulsecart-notification-group")
    public void onOrderEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "";
            JsonNode payload = root.get("payload");

            if (payload == null) {
                return;
            }

            String orderId = payload.has("orderId") ? payload.get("orderId").asText() : "";
            String customerId = payload.has("customerId") ? payload.get("customerId").asText() : "customer";
            String totalAmount = payload.has("totalAmount") ? payload.get("totalAmount").asText() : "0.00";

            if ("OrderConfirmed".equalsIgnoreCase(eventType) || "order.confirmed".equalsIgnoreCase(eventType)) {
                log.info("Notification service processing order confirmation for order {}", orderId);
                SendNotificationRequest request = new SendNotificationRequest(
                        customerId + "@pulsecart.io",
                        NotificationChannel.EMAIL,
                        "ORDER_CONFIRMED",
                        "Your PulseCart Order " + orderId + " is Confirmed!",
                        Map.of(
                                "customerName", customerId,
                                "orderId", orderId,
                                "totalAmount", totalAmount
                        ),
                        orderId
                );
                notificationService.sendNotification(request);
            } else if ("OrderCancelled".equalsIgnoreCase(eventType) || "order.cancelled".equalsIgnoreCase(eventType)) {
                log.info("Notification service processing order cancellation for order {}", orderId);
                SendNotificationRequest request = new SendNotificationRequest(
                        customerId + "@pulsecart.io",
                        NotificationChannel.EMAIL,
                        "ORDER_CANCELLED",
                        "Update: PulseCart Order " + orderId + " Cancelled",
                        Map.of(
                                "customerName", customerId,
                                "orderId", orderId
                        ),
                        orderId
                );
                notificationService.sendNotification(request);
            }
        } catch (Exception e) {
            log.warn("Failed processing order event in notification listener: {}", e.getMessage());
            notificationService.recordDlqMessage(
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.value(),
                    e.getMessage()
            );
        }
    }

    @KafkaListener(topics = PulseCartTopics.SHIPPING_EVENTS, groupId = "pulsecart-notification-group")
    public void onShippingEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "";
            JsonNode payload = root.get("payload");

            if (payload == null) {
                return;
            }

            String orderId = payload.has("orderId") ? payload.get("orderId").asText() : "";
            String customerId = payload.has("customerId") ? payload.get("customerId").asText() : "customer";
            String carrier = payload.has("carrier") ? payload.get("carrier").asText() : "Standard Carrier";
            String trackingNumber = payload.has("trackingNumber") ? payload.get("trackingNumber").asText() : "N/A";

            if ("ShippingDispatched".equalsIgnoreCase(eventType) || "shipping.dispatched".equalsIgnoreCase(eventType)) {
                log.info("Notification service processing shipment dispatch for order {}", orderId);
                SendNotificationRequest request = new SendNotificationRequest(
                        customerId + "@pulsecart.io",
                        NotificationChannel.EMAIL,
                        "SHIPMENT_DISPATCHED",
                        "Your PulseCart Order " + orderId + " Has Shipped!",
                        Map.of(
                                "customerName", customerId,
                                "orderId", orderId,
                                "carrier", carrier,
                                "trackingNumber", trackingNumber
                        ),
                        orderId
                );
                notificationService.sendNotification(request);
            }
        } catch (Exception e) {
            log.warn("Failed processing shipping event in notification listener: {}", e.getMessage());
            notificationService.recordDlqMessage(
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.value(),
                    e.getMessage()
            );
        }
    }

    @KafkaListener(topics = PulseCartTopics.NOTIFICATION_DLQ, groupId = "pulsecart-notification-dlq-consumer")
    public void onDlqMessage(ConsumerRecord<String, String> record) {
        log.warn("Direct DLQ message received on topic {}: {}", record.topic(), record.value());
        notificationService.recordDlqMessage(
                record.topic(),
                record.partition(),
                record.offset(),
                record.value(),
                "Direct DLQ ingestion"
        );
    }
}
