package com.pulsecart.promotion.specification;

public interface PromotionSpecification {
    ValidationResult validate(PromotionContext context);

    default PromotionSpecification and(PromotionSpecification other) {
        return context -> {
            ValidationResult first = this.validate(context);
            if (!first.valid()) {
                return first;
            }
            return other.validate(context);
        };
    }
}
