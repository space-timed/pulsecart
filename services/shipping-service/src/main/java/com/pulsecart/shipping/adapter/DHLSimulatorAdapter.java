package com.pulsecart.shipping.adapter;

import com.pulsecart.shipping.domain.ShippingCarrier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class DHLSimulatorAdapter implements CarrierAdapter {

    @Override
    public ShippingCarrier getCarrier() {
        return ShippingCarrier.DHL;
    }

    @Override
    public CarrierDispatchResult dispatch(CarrierDispatchCommand command) {
        String tracking = "DHL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        Instant estimatedDelivery = Instant.now().plus(2, ChronoUnit.DAYS);
        return CarrierDispatchResult.success(tracking, estimatedDelivery);
    }
}
