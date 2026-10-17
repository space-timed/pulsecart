package com.pulsecart.promotion.specification;

public class ActiveStatusSpecification implements PromotionSpecification {
    @Override
    public ValidationResult validate(PromotionContext context) {
        if (!context.coupon().isActive()) {
            return ValidationResult.invalid("Coupon is deactivated");
        }
        return ValidationResult.ok();
    }
}
