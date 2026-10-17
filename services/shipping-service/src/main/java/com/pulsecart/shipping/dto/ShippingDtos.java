package com.pulsecart.shipping.dto;

import com.pulsecart.shipping.domain.ShipmentStatus;
import com.pulsecart.shipping.domain.ShippingCarrier;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class ShippingDtos {

    private ShippingDtos() {}

    public record DispatchShipmentRequest(
            @NotBlank String orderId,
            @NotBlank String customerId,
            ShippingCarrier carrier,
            String destinationAddress
    ) {}

    public record ShipmentResponse(
            String id,
            String orderId,
            String customerId,
            ShippingCarrier carrier,
            String trackingNumber,
            ShipmentStatus status,
            String destinationAddress,
            Instant estimatedDeliveryAt,
            Instant dispatchedAt,
            Instant deliveredAt,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
