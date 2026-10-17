package com.pulsecart.pricing.strategy;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class VipTierPricingStrategy implements PricingStrategy {

    private static final Map<String, BigDecimal> TIER_DISCOUNTS = Map.of(
            "PLATINUM", new BigDecimal("0.20"),
            "GOLD", new BigDecimal("0.15"),
            "SILVER", new BigDecimal("0.10"),
            "BRONZE", new BigDecimal("0.05")
    );

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.VIP_TIER;
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        if (context.customerTier() == null) return false;
        String tier = context.customerTier().toUpperCase();
        return TIER_DISCOUNTS.containsKey(tier);
    }

    @Override
    public PricingCalculationResult calculate(PricingContext context) {
        String tier = context.customerTier().toUpperCase();
        BigDecimal discountRate = TIER_DISCOUNTS.getOrDefault(tier, BigDecimal.ZERO);

        BigDecimal base = context.basePrice().setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal discountPerUnit = base.multiply(discountRate).setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal effectiveUnit = base.subtract(discountPerUnit).setScale(4, RoundingMode.HALF_EVEN);

        BigDecimal subtotal = base.multiply(BigDecimal.valueOf(context.quantity())).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal finalTotal = effectiveUnit.multiply(BigDecimal.valueOf(context.quantity())).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal totalDiscount = subtotal.subtract(finalTotal).setScale(2, RoundingMode.HALF_EVEN);

        return new PricingCalculationResult(
                base,
                effectiveUnit,
                context.quantity(),
                subtotal,
                totalDiscount,
                finalTotal,
                PricingStrategyType.VIP_TIER,
                String.format("VIP %s loyalty member benefit applied: %.0f%% price reduction.", tier, discountRate.multiply(BigDecimal.valueOf(100)).doubleValue())
        );
    }
}
