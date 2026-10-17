package com.pulsecart.pricing.dto;

import com.pulsecart.pricing.strategy.PricingCalculationResult;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PricingDtos {

    private PricingDtos() {}

    public record SetPriceRequest(
            @NotBlank(message = "Product ID is mandatory")
            String productId,
            @NotNull(message = "Base price is mandatory")
            @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
            BigDecimal basePrice,
            String currency,
            String reason,
            String changedBy
    ) {}

    public record CalculatePriceRequest(
            @NotBlank(message = "Product ID is mandatory")
            String productId,
            @Min(value = 1, message = "Quantity must be at least 1")
            int quantity,
            String customerTier,
            boolean isFlashSale,
            BigDecimal promoDiscountPercent
    ) {}

    public record PriceResponse(
            String id,
            String productId,
            BigDecimal basePrice,
            String currency,
            boolean isActive,
            Instant updatedAt
    ) {}

    public record PriceHistoryResponse(
            String id,
            String productId,
            BigDecimal oldPrice,
            BigDecimal newPrice,
            String currency,
            String changedBy,
            String reason,
            Instant createdAt
    ) {}
}
