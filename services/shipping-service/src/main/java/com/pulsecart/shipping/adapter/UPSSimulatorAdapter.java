package com.pulsecart.shipping.adapter;

import com.pulsecart.shipping.domain.ShippingCarrier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class UPSSimulatorAdapter implements CarrierAdapter {

    @Override
    public ShippingCarrier getCarrier() {
        return ShippingCarrier.UPS;
    }

    @Override
    public CarrierDispatchResult dispatch(CarrierDispatchCommand command) {
        String tracking = "1Z-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
        Instant estimatedDelivery = Instant.now().plus(4, ChronoUnit.DAYS);
        return CarrierDispatchResult.success(tracking, estimatedDelivery);
    }
}
