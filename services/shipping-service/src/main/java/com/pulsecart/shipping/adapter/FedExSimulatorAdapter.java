package com.pulsecart.shipping.adapter;

import com.pulsecart.shipping.domain.ShippingCarrier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class FedExSimulatorAdapter implements CarrierAdapter {

    @Override
    public ShippingCarrier getCarrier() {
        return ShippingCarrier.FEDEX;
    }

    @Override
    public CarrierDispatchResult dispatch(CarrierDispatchCommand command) {
        String tracking = "FDX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        Instant estimatedDelivery = Instant.now().plus(3, ChronoUnit.DAYS);
        return CarrierDispatchResult.success(tracking, estimatedDelivery);
    }
}
