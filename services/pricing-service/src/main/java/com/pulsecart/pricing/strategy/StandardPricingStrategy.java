package com.pulsecart.pricing.strategy;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.STANDARD;
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        return true; // Fallback default strategy
    }

    @Override
    public PricingCalculationResult calculate(PricingContext context) {
        BigDecimal unitPrice = context.basePrice().setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(context.quantity())).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        return new PricingCalculationResult(
                unitPrice,
                unitPrice,
                context.quantity(),
                subtotal,
                discount,
                subtotal,
                PricingStrategyType.STANDARD,
                "Standard retail catalog pricing applied."
        );
    }
}
