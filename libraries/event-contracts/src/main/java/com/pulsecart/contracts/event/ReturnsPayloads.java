package com.pulsecart.contracts.event;

import java.math.BigDecimal;
import java.util.List;

public final class ReturnsPayloads {
    private ReturnsPayloads() {}

    public record ReturnItem(
            String productId,
            int quantity
    ) {}

    public record ReturnRequestedPayload(
            String returnId,
            String orderId,
            String customerId,
            String reason,
            List<ReturnItem> items
    ) {}

    public record ReturnApprovedPayload(
            String returnId,
            String orderId,
            BigDecimal estimatedRefundAmount,
            String rmaNumber
    ) {}
}
