package com.pulsecart.pricing.strategy;

public interface PricingStrategy {
    PricingStrategyType getType();
    boolean isApplicable(PricingContext context);
    PricingCalculationResult calculate(PricingContext context);
}
