package com.pulsecart.shipping.adapter;

public record CarrierDispatchCommand(
        String shipmentId,
        String orderId,
        String destinationAddress
) {}
