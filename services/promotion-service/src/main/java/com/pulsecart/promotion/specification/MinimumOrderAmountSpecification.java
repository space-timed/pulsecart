package com.pulsecart.promotion.specification;

import java.math.BigDecimal;

public class MinimumOrderAmountSpecification implements PromotionSpecification {
    @Override
    public ValidationResult validate(PromotionContext context) {
        BigDecimal minAmount = context.coupon().getMinOrderAmount();
        if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (context.orderAmount() == null || context.orderAmount().compareTo(minAmount) < 0) {
                return ValidationResult.invalid("Order subtotal of $" + context.orderAmount() +
                        " does not meet minimum order requirement of $" + minAmount);
            }
        }
        return ValidationResult.ok();
    }
}
