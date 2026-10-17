package com.pulsecart.pricing.strategy;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class VolumeDiscountPricingStrategy implements PricingStrategy {

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.VOLUME_DISCOUNT;
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        return context.quantity() >= 5;
    }

    @Override
    public PricingCalculationResult calculate(PricingContext context) {
        BigDecimal rate = context.quantity() >= 10 ? new BigDecimal("0.12") : new BigDecimal("0.07");

        BigDecimal base = context.basePrice().setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal discountPerUnit = base.multiply(rate).setScale(4, RoundingMode.HALF_EVEN);
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
                PricingStrategyType.VOLUME_DISCOUNT,
                String.format("Bulk purchase volume discount applied for %d units (%.0f%% discount).", context.quantity(), rate.multiply(BigDecimal.valueOf(100)).doubleValue())
        );
    }
}
