package com.pulsecart.contracts.event;

import java.math.BigDecimal;
import java.util.List;

public final class OrderPayloads {

    private OrderPayloads() {}

    public record OrderItemPayload(
            String productId,
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {}

    public record OrderCreatedPayload(
            String orderId,
            String customerId,
            BigDecimal totalAmount,
            String currency,
            List<OrderItemPayload> items
    ) {}

    public record OrderConfirmedPayload(
            String orderId,
            String customerId,
            BigDecimal totalAmount,
            String currency,
            String paymentTransactionId,
            List<OrderItemPayload> items
    ) {}

    public record OrderCancelledPayload(
            String orderId,
            String customerId,
            String reason,
            boolean inventoryReleased
    ) {}
}
