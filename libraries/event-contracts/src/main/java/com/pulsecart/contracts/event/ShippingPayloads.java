package com.pulsecart.contracts.event;

public final class ShippingPayloads {
    private ShippingPayloads() {}

    public record ShipmentDispatchedPayload(
            String shipmentId,
            String orderId,
            String carrier,
            String trackingNumber,
            String estimatedDeliveryDate
    ) {}

    public record ShipmentDeliveredPayload(
            String shipmentId,
            String orderId,
            String deliveredAt
    ) {}
}
