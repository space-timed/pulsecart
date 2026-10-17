package com.pulsecart.promotion.specification;

public record ValidationResult(boolean valid, String message) {
    public static ValidationResult ok() {
        return new ValidationResult(true, "Promotion is valid");
    }

    public static ValidationResult invalid(String message) {
        return new ValidationResult(false, message);
    }
}
