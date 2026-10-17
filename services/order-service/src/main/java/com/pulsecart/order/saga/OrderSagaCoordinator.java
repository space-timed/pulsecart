package com.pulsecart.order.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.EventEnvelope;
import com.pulsecart.contracts.event.OrderPayloads.*;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.order.client.InventoryClient;
import com.pulsecart.order.client.PaymentClient;
import com.pulsecart.order.domain.*;
import com.pulsecart.order.dto.OrderDtos.*;
import com.pulsecart.order.repository.OrderRepository;
import com.pulsecart.order.repository.OutboxEventRepository;
import com.pulsecart.order.repository.SagaInstanceRepository;
import com.pulsecart.order.repository.SagaStepLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderSagaCoordinator {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaCoordinator.class);

    private final OrderRepository orderRepository;
    private final SagaInstanceRepository sagaRepository;
    private final SagaStepLogRepository stepLogRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final ObjectMapper objectMapper;

    public OrderSagaCoordinator(OrderRepository orderRepository,
                                SagaInstanceRepository sagaRepository,
                                SagaStepLogRepository stepLogRepository,
                                OutboxEventRepository outboxEventRepository,
                                InventoryClient inventoryClient,
                                PaymentClient paymentClient,
                                ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.sagaRepository = sagaRepository;
        this.stepLogRepository = stepLogRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CheckoutResponse executeSaga(CheckoutRequest request, String idempotencyKey) {
        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Order> existingOpt = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOpt.isPresent()) {
                Order existing = existingOpt.get();
                log.info("Idempotent checkout hit for key: {}, returning existing order: {}", idempotencyKey, existing.getId());
                return toCheckoutResponse(existing, SagaStatus.SUCCEEDED, "Idempotent duplicate request returned");
            }
        }

        // 2. Initialize Order & Calculate Totals
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        List<InventoryClient.ReservationItemDto> inventoryItems = new ArrayList<>();

        for (CheckoutItemRequest itemReq : request.items()) {
            BigDecimal itemSubtotal = itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity())).setScale(2, RoundingMode.HALF_EVEN);
            subtotal = subtotal.add(itemSubtotal);
            items.add(new OrderItem(itemReq.productId(), itemReq.sku(), itemReq.productName(), itemReq.unitPrice(), itemReq.quantity(), itemSubtotal));
            inventoryItems.add(new InventoryClient.ReservationItemDto(itemReq.productId(), itemReq.quantity()));
        }

        BigDecimal discountAmount = BigDecimal.ZERO; // Could be computed via PromotionClient
        BigDecimal totalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_EVEN);

        String orderId = "ord_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        Order order = new Order(orderId, request.customerId(), subtotal, discountAmount, totalAmount, "USD",
                request.shippingAddress(), idempotencyKey);
        for (OrderItem item : items) {
            order.addItem(item);
        }
        orderRepository.save(order);

        // 3. Initialize Saga Instance
        String sagaId = "saga_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        SagaInstance saga = new SagaInstance(sagaId, orderId, "INITIATED");
        sagaRepository.save(saga);

        // =========================================================================
        // SAGA STEP 1: RESERVE INVENTORY
        // =========================================================================
        saga.setCurrentStep("RESERVE_INVENTORY");
        InventoryClient.ReservationResult resResult = inventoryClient.reserveStock(orderId, inventoryItems);

        if (!resResult.success()) {
            // STEP 1 FAILED -> Cancel order directly (nothing to compensate)
            recordStepLog(saga, "RESERVE_INVENTORY", "FAILED", null, resResult.errorMessage());
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancellationReason("INSUFFICIENT_STOCK: " + resResult.errorMessage());
            orderRepository.save(order);

            saga.setStatus(SagaStatus.FAILED);
            saga.setCompletedAt(Instant.now());
            sagaRepository.save(saga);

            createOutboxEvent("Order", orderId, "OrderCancelled", PulseCartTopics.ORDER_EVENTS,
                    MapOf("orderId", orderId, "reason", order.getCancellationReason()));

            return toCheckoutResponse(order, SagaStatus.FAILED, "Checkout failed: Stock unavailable");
        }

        // Step 1 Success
        recordStepLog(saga, "RESERVE_INVENTORY", "SUCCESS", resResult.reservationId(), null);
        order.setReservationId(resResult.reservationId());
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);

        // =========================================================================
        // SAGA STEP 2: AUTHORIZE & CAPTURE PAYMENT
        // =========================================================================
        saga.setCurrentStep("AUTHORIZE_PAYMENT");
        PaymentClient.PaymentAuthResult payResult = paymentClient.authorizePayment(
                orderId, request.customerId(), totalAmount, request.paymentMethodToken(), request.chaosPaymentAction()
        );

        if (!payResult.success()) {
            // STEP 2 FAILED -> TRIGGER SAGA COMPENSATION (Release inventory reservation)
            recordStepLog(saga, "AUTHORIZE_PAYMENT", "FAILED", null, payResult.errorMessage());
            log.warn("Payment authorization failed for order {}: {}. Triggering compensation...", orderId, payResult.errorMessage());

            saga.setStatus(SagaStatus.COMPENSATING);
            saga.setCompensationReason(payResult.errorMessage());
            order.setStatus(OrderStatus.COMPENSATING_INVENTORY);
            orderRepository.save(order);

            // COMPENSATION ACTION: Release reserved inventory
            boolean released = inventoryClient.releaseStock(order.getReservationId());
            if (released) {
                recordStepLog(saga, "COMPENSATE_INVENTORY_RELEASE", "SUCCESS", order.getReservationId(), null);
            } else {
                recordStepLog(saga, "COMPENSATE_INVENTORY_RELEASE", "FAILED", order.getReservationId(), "Failed to release reservation");
            }

            order.setStatus(OrderStatus.CANCELLED);
            order.setCancellationReason("PAYMENT_FAILED: " + payResult.errorMessage());
            orderRepository.save(order);

            saga.setStatus(SagaStatus.FAILED);
            saga.setCompletedAt(Instant.now());
            sagaRepository.save(saga);

            createOutboxEvent("Order", orderId, "OrderCancelled", PulseCartTopics.ORDER_EVENTS,
                    MapOf("orderId", orderId, "reason", order.getCancellationReason()));

            return toCheckoutResponse(order, SagaStatus.FAILED, "Checkout failed: Payment declined. Stock released.");
        }

        // Step 2 Success
        recordStepLog(saga, "AUTHORIZE_PAYMENT", "SUCCESS", payResult.paymentId(), null);
        order.setPaymentId(payResult.paymentId());

        // Commit inventory reservation
        inventoryClient.commitStock(order.getReservationId());

        // =========================================================================
        // SAGA COMPLETION: CONFIRM ORDER & WRITE TRANSACTIONAL OUTBOX
        // =========================================================================
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        saga.setCurrentStep("COMPLETED");
        saga.setStatus(SagaStatus.SUCCEEDED);
        saga.setCompletedAt(Instant.now());
        sagaRepository.save(saga);

        // Transactional Outbox Event for Kafka Topic
        List<OrderItemPayload> itemPayloads = order.getItems().stream()
                .map(i -> new OrderItemPayload(i.getProductId(), i.getSku(), i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        OrderConfirmedPayload confirmedPayload = new OrderConfirmedPayload(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getPaymentId(),
                itemPayloads
        );

        EventEnvelope<OrderConfirmedPayload> envelope = EventEnvelope.create(
                "order.confirmed",
                1,
                order.getId(),
                "Order",
                null,
                null,
                "order-service",
                confirmedPayload
        );

        try {
            String envelopeJson = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = new OutboxEvent("Order", order.getId(), "OrderConfirmed", PulseCartTopics.ORDER_EVENTS, envelopeJson);
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize OrderConfirmed event: {}", e.getMessage());
        }

        return toCheckoutResponse(order, SagaStatus.SUCCEEDED, "Checkout completed successfully");
    }

    private void recordStepLog(SagaInstance saga, String stepName, String status, String payload, String error) {
        SagaStepLog stepLog = new SagaStepLog(saga, stepName, status, payload, error);
        stepLogRepository.save(stepLog);
    }

    private void createOutboxEvent(String aggregateType, String aggregateId, String eventType, String topic, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            OutboxEvent event = new OutboxEvent(aggregateType, aggregateId, eventType, topic, json);
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event: {}", e.getMessage());
        }
    }

    private CheckoutResponse toCheckoutResponse(Order o, SagaStatus sagaStatus, String message) {
        return new CheckoutResponse(
                o.getId(),
                o.getStatus(),
                o.getSubtotal(),
                o.getDiscountAmount(),
                o.getTotalAmount(),
                o.getReservationId(),
                o.getPaymentId(),
                o.getCancellationReason(),
                sagaStatus,
                message
        );
    }

    private java.util.Map<String, String> MapOf(String k1, String v1, String k2, String v2) {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
}
