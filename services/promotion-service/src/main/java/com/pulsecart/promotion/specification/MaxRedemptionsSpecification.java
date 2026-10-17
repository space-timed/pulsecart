package com.pulsecart.promotion.specification;

public class MaxRedemptionsSpecification implements PromotionSpecification {
    @Override
    public ValidationResult validate(PromotionContext context) {
        if (context.coupon().getCurrentRedemptions() >= context.coupon().getMaxRedemptions()) {
            return ValidationResult.invalid("Coupon redemption limit has been reached");
        }
        return ValidationResult.ok();
    }
}
