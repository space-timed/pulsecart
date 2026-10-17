package com.pulsecart.shipping.adapter;

import com.pulsecart.shipping.domain.ShippingCarrier;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CarrierAdapterFactory {

    private final Map<ShippingCarrier, CarrierAdapter> adapterMap = new EnumMap<>(ShippingCarrier.class);

    public CarrierAdapterFactory(List<CarrierAdapter> adapters) {
        for (CarrierAdapter adapter : adapters) {
            adapterMap.put(adapter.getCarrier(), adapter);
        }
    }

    public CarrierAdapter getAdapter(ShippingCarrier carrier) {
        CarrierAdapter adapter = adapterMap.get(carrier);
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported shipping carrier: " + carrier);
        }
        return adapter;
    }
}
