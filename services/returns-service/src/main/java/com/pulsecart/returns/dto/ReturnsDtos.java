package com.pulsecart.returns.dto;

import com.pulsecart.returns.domain.ReturnStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ReturnsDtos {

    private ReturnsDtos() {}

    public record CreateReturnRequest(
            @NotBlank String orderId,
            @NotBlank String customerId,
            @NotNull Instant deliveryDate,
            @NotBlank String reason,
            @NotEmpty @Valid List<ReturnItemRequest> items
    ) {}

    public record ReturnItemRequest(
            @NotBlank String productId,
            @Min(1) int quantity,
            String condition,
            @NotNull @DecimalMin("0.00") BigDecimal price
    ) {}

    public record ProcessReturnReceiptRequest(
            String inspectionCondition,
            String notes
    ) {}

    public record ReturnItemResponse(
            Long id,
            String productId,
            int quantity,
            String condition,
            BigDecimal price
    ) {}

    public record ReturnResponse(
            String id,
            String orderId,
            String customerId,
            ReturnStatus status,
            String reason,
            BigDecimal refundAmount,
            List<ReturnItemResponse> items,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
