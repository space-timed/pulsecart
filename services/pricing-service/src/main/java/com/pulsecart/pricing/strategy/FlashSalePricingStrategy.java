package com.pulsecart.pricing.strategy;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FlashSalePricingStrategy implements PricingStrategy {

    private static final BigDecimal DEFAULT_FLASH_DISCOUNT = new BigDecimal("0.30"); // 30% off

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.FLASH_SALE;
    }

    @Override
    public boolean isApplicable(PricingContext context) {
        return context.isFlashSale();
    }

    @Override
    public PricingCalculationResult calculate(PricingContext context) {
        BigDecimal discountPercent = context.promoDiscountPercent() != null
                ? context.promoDiscountPercent()
                : DEFAULT_FLASH_DISCOUNT;

        BigDecimal base = context.basePrice().setScale(4, RoundingMode.HALF_EVEN);
        BigDecimal discountPerUnit = base.multiply(discountPercent).setScale(4, RoundingMode.HALF_EVEN);
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
                PricingStrategyType.FLASH_SALE,
                String.format("Flash-sale event pricing applied: %.0f%% promotional discount.", discountPercent.multiply(BigDecimal.valueOf(100)).doubleValue())
        );
    }
}
