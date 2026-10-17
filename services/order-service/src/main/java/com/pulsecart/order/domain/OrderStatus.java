package com.pulsecart.order.domain;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    CONFIRMED,
    COMPENSATING_INVENTORY,
    CANCELLED
}
