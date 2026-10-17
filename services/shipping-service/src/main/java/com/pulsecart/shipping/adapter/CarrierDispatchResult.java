package com.pulsecart.shipping.adapter;

import java.time.Instant;

public record CarrierDispatchResult(
        boolean success,
        String trackingNumber,
        Instant estimatedDeliveryAt,
        String errorMessage
) {
    public static CarrierDispatchResult success(String trackingNumber, Instant estimatedDeliveryAt) {
        return new CarrierDispatchResult(true, trackingNumber, estimatedDeliveryAt, null);
    }

    public static CarrierDispatchResult failure(String errorMessage) {
        return new CarrierDispatchResult(false, null, null, errorMessage);
    }
}
