package com.pulsecart.cart.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class CartDtos {

    private CartDtos() {}

    public record AddCartItemRequest(
            @NotBlank(message = "Product ID is mandatory")
            String productId,
            @NotBlank(message = "SKU is mandatory")
            String sku,
            @NotBlank(message = "Product name is mandatory")
            String productName,
            @Min(value = 1, message = "Quantity must be at least 1")
            int quantity,
            @NotNull(message = "Estimated unit price is mandatory")
            @DecimalMin(value = "0.01", message = "Price must be positive")
            BigDecimal estimatedUnitPrice,
            String imageUrl
    ) {}

    public record UpdateQuantityRequest(
            @Min(value = 0, message = "Quantity cannot be negative")
            int quantity
    ) {}

    public record CartItemResponse(
            String productId,
            String sku,
            String productName,
            int quantity,
            BigDecimal estimatedUnitPrice,
            BigDecimal lineTotal,
            String imageUrl
    ) {}

    public record CartResponse(
            String customerId,
            List<CartItemResponse> items,
            BigDecimal totalEstimatedAmount,
            int totalItemCount,
            Instant updatedAt
    ) {}
}
