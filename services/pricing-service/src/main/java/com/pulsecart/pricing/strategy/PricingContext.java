package com.pulsecart.pricing.strategy;

import java.math.BigDecimal;

public record PricingContext(
        String productId,
        BigDecimal basePrice,
        int quantity,
        String customerTier,     // STANDARD, BRONZE, SILVER, GOLD, PLATINUM
        boolean isFlashSale,
        BigDecimal promoDiscountPercent // Optional percentage e.g. 0.20 for 20%
) {}
