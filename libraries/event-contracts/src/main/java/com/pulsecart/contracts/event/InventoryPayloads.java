package com.pulsecart.contracts.event;

import java.util.List;

public final class InventoryPayloads {

    private InventoryPayloads() {}

    public record ReservationItem(
            String productId,
            int quantity
    ) {}

    public record StockReservedPayload(
            String reservationId,
            String orderId,
            List<ReservationItem> items
    ) {}

    public record StockReservationFailedPayload(
            String orderId,
            String reason,
            List<ReservationItem> unfulfillableItems
    ) {}

    public record StockReleasedPayload(
            String reservationId,
            String orderId,
            String reason
    ) {}
}
