package com.pulsecart.order.dto;

import com.pulsecart.order.domain.OrderStatus;
import com.pulsecart.order.domain.SagaStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {}

    public record CheckoutItemRequest(
            @NotBlank String productId,
            @NotBlank String sku,
            @NotBlank String productName,
            @NotNull @DecimalMin("0.01") BigDecimal unitPrice,
            int quantity
    ) {}

    public record CheckoutRequest(
            @NotBlank String customerId,
            @NotEmpty List<CheckoutItemRequest> items,
            String shippingAddress,
            String paymentMethodToken,
            String promoCode,
            String chaosPaymentAction
    ) {}

    public record CheckoutResponse(
            String orderId,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            String reservationId,
            String paymentId,
            String cancellationReason,
            SagaStatus sagaStatus,
            String message
    ) {}

    public record OrderItemDto(
            String productId,
            String sku,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal
    ) {}

    public record OrderDetailResponse(
            String orderId,
            String customerId,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            String currency,
            String reservationId,
            String paymentId,
            String cancellationReason,
            String shippingAddress,
            List<OrderItemDto> items,
            Instant createdAt,
            Instant updatedAt
    ) {}

    public record SagaStepLogDto(
            String stepName,
            String status,
            String payload,
            String errorMessage,
            Instant executedAt
    ) {}

    public record SagaInstanceDto(
            String id,
            String orderId,
            String currentStep,
            SagaStatus status,
            String compensationReason,
            List<SagaStepLogDto> stepLogs,
            Instant startedAt,
            Instant completedAt
    ) {}
}
