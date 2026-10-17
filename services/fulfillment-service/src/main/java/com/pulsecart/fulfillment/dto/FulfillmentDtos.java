package com.pulsecart.fulfillment.dto;

import com.pulsecart.fulfillment.domain.FulfillmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.List;

public final class FulfillmentDtos {

    private FulfillmentDtos() {}

    public record FulfillmentItemRequest(
            @NotBlank String productId,
            @NotBlank String sku,
            @NotBlank String productName,
            int quantity
    ) {}

    public record ProcessFulfillmentRequest(
            @NotBlank String orderId,
            @NotBlank String customerId,
            String shippingAddress,
            @NotEmpty List<FulfillmentItemRequest> items
    ) {}

    public record FulfillmentItemDto(
            String productId,
            String sku,
            String productName,
            int quantity
    ) {}

    public record FulfillmentResponse(
            String id,
            String orderId,
            String customerId,
            String warehouseId,
            FulfillmentStatus status,
            String shippingAddress,
            List<FulfillmentItemDto> items,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
