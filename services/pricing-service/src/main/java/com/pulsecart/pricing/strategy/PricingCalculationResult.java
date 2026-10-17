package com.pulsecart.pricing.strategy;

import java.math.BigDecimal;

public record PricingCalculationResult(
        BigDecimal originalUnitPrice,
        BigDecimal effectiveUnitPrice,
        int quantity,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal finalTotal,
        PricingStrategyType appliedStrategy,
        String explanation
) {}
