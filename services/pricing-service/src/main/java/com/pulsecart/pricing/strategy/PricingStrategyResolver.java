package com.pulsecart.pricing.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PricingStrategyResolver {

    private final List<PricingStrategy> strategies;

    public PricingStrategyResolver(List<PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    public PricingStrategy resolve(PricingContext context) {
        return strategies.stream()
                .filter(s -> s.isApplicable(context))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No applicable pricing strategy found"));
    }
}
