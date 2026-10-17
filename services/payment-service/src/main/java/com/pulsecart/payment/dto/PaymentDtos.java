package com.pulsecart.payment.dto;

import com.pulsecart.payment.domain.PaymentProvider;
import com.pulsecart.payment.domain.PaymentStatus;
import com.pulsecart.payment.domain.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PaymentDtos {

    private PaymentDtos() {}

    public record AuthorizePaymentRequest(
            @NotBlank String orderId,
            @NotBlank String customerId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            String currency,
            PaymentProvider provider,
            String paymentMethodToken,
            String chaosAction
    ) {}

    public record AuthorizePaymentResponse(
            String paymentId,
            String orderId,
            PaymentStatus status,
            BigDecimal amount,
            String currency,
            String authorizationCode,
            String transactionReference,
            String errorMessage
    ) {}

    public record CapturePaymentResponse(
            String paymentId,
            String orderId,
            PaymentStatus status,
            BigDecimal amount,
            String captureReference,
            String message
    ) {}

    public record RefundPaymentRequest(
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            String reason
    ) {}

    public record RefundPaymentResponse(
            String paymentId,
            String orderId,
            PaymentStatus status,
            BigDecimal refundedAmount,
            String refundReference,
            String message
    ) {}

    public record PaymentTransactionDto(
            Long id,
            TransactionType type,
            BigDecimal amount,
            String status,
            String gatewayReference,
            String errorMessage,
            Instant createdAt
    ) {}

    public record PaymentDetailResponse(
            String paymentId,
            String orderId,
            String customerId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            PaymentProvider provider,
            String authorizationCode,
            String failureReason,
            List<PaymentTransactionDto> transactions,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
