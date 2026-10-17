package com.pulsecart.order.client;

import java.util.List;

public interface InventoryClient {

    record ReservationItemDto(String productId, int quantity) {}
    record ReservationResult(boolean success, String reservationId, String errorMessage) {}

    ReservationResult reserveStock(String orderId, List<ReservationItemDto> items);
    boolean commitStock(String reservationId);
    boolean releaseStock(String reservationId);
}
