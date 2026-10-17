package com.pulsecart.shipping.adapter;

import com.pulsecart.shipping.domain.ShippingCarrier;

public interface CarrierAdapter {
    ShippingCarrier getCarrier();
    CarrierDispatchResult dispatch(CarrierDispatchCommand command);
}
