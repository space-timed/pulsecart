package com.pulsecart.promotion.specification;

public class CustomerTierSpecification implements PromotionSpecification {
    @Override
    public ValidationResult validate(PromotionContext context) {
        String requiredTier = context.coupon().getCustomerTierRequired();
        if (requiredTier != null && !requiredTier.isBlank()) {
            if (context.customerTier() == null || !context.customerTier().equalsIgnoreCase(requiredTier)) {
                return ValidationResult.invalid("Coupon requires customer tier " + requiredTier +
                        ", but current tier is " + (context.customerTier() != null ? context.customerTier() : "STANDARD"));
            }
        }
        return ValidationResult.ok();
    }
}
