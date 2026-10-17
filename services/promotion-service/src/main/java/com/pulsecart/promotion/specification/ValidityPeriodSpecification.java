package com.pulsecart.promotion.specification;

import java.time.Instant;

public class ValidityPeriodSpecification implements PromotionSpecification {
    @Override
    public ValidationResult validate(PromotionContext context) {
        Instant now = Instant.now();
        if (now.isBefore(context.coupon().getStartsAt())) {
            return ValidationResult.invalid("Coupon promotion has not started yet");
        }
        if (now.isAfter(context.coupon().getExpiresAt())) {
            return ValidationResult.invalid("Coupon has expired");
        }
        return ValidationResult.ok();
    }
}
