package com.pulsecart.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public final class InventoryDtos {

    private InventoryDtos() {}

    public record CreateInventoryRequest(
            @NotBlank(message = "Product ID is mandatory")
            String productId,
            @NotBlank(message = "SKU is mandatory")
            String sku,
            @Min(value = 0, message = "Initial stock cannot be negative")
            int initialStock
    ) {}

    public record RestockRequest(
            @Min(value = 1, message = "Restock quantity must be at least 1")
            int additionalQuantity
    ) {}

    public record ReservationItemDto(
            @NotBlank(message = "Product ID is mandatory")
            String productId,
            @Min(value = 1, message = "Quantity must be at least 1")
            int quantity
    ) {}

    public record ReserveStockRequest(
            @NotBlank(message = "Order ID is mandatory")
            String orderId,
            @NotEmpty(message = "Reservation items cannot be empty")
            List<ReservationItemDto> items,
            int ttlSeconds
    ) {}

    public record ReleaseStockRequest(
            @NotBlank(message = "Order ID is mandatory")
            String orderId,
            String reason
    ) {}

    public record CommitStockRequest(
            @NotBlank(message = "Order ID is mandatory")
            String orderId
    ) {}

    public record InventoryResponse(
            String id,
            String productId,
            String sku,
            int availableQuantity,
            int reservedQuantity,
            int totalQuantity,
            Instant updatedAt
    ) {}

    public record ReservationResponse(
            String reservationId,
            String orderId,
            String status,
            Instant expiresAt,
            List<ReservationItemDto> items
    ) {}
}
