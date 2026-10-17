package com.pulsecart.contracts.event;

import java.math.BigDecimal;

public final class PaymentPayloads {

    private PaymentPayloads() {}

    public record PaymentAuthorizedPayload(
            String paymentId,
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            String authorizationCode,
            String idempotencyKey
    ) {}

    public record PaymentAuthFailedPayload(
            String paymentId,
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            String failureReason,
            String errorCode
    ) {}

    public record PaymentCapturedPayload(
            String paymentId,
            String orderId,
            BigDecimal amount,
            String transactionRef
    ) {}

    public record PaymentRefundedPayload(
            String refundId,
            String paymentId,
            String orderId,
            BigDecimal refundAmount,
            String reason
    ) {}
}
