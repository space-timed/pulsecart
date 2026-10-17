package com.pulsecart.promotion.dto;

import com.pulsecart.promotion.domain.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public final class PromotionDtos {

    private PromotionDtos() {}

    public record CreateCouponRequest(
            @NotBlank String code,
            String description,
            @NotNull DiscountType discountType,
            @NotNull @DecimalMin("0.01") BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            int maxRedemptions,
            @NotNull Instant startsAt,
            @NotNull Instant expiresAt,
            String customerTierRequired
    ) {}

    public record CouponResponse(
            Long id,
            String code,
            String description,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            int maxRedemptions,
            int currentRedemptions,
            Instant startsAt,
            Instant expiresAt,
            boolean active,
            String customerTierRequired
    ) {}

    public record ValidateCouponRequest(
            @NotBlank String code,
            @NotNull BigDecimal orderAmount,
            String customerId,
            String customerTier
    ) {}

    public record CouponValidationResponse(
            boolean valid,
            String code,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal calculatedDiscount,
            BigDecimal finalOrderAmount,
            String message
    ) {}

    public record ApplyCouponRequest(
            @NotBlank String code,
            @NotBlank String orderId,
            @NotNull BigDecimal orderAmount,
            @NotBlank String customerId,
            String customerTier
    ) {}

    public record CouponRedemptionResponse(
            boolean success,
            String couponCode,
            String orderId,
            BigDecimal discountApplied,
            BigDecimal finalOrderAmount,
            String message
    ) {}
}
