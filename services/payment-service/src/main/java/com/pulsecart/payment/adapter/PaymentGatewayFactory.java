package com.pulsecart.payment.adapter;

import com.pulsecart.payment.domain.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayFactory {

    private final Map<PaymentProvider, PaymentGateway> gatewayMap = new EnumMap<>(PaymentProvider.class);

    public PaymentGatewayFactory(List<PaymentGateway> gateways) {
        for (PaymentGateway gateway : gateways) {
            gatewayMap.put(gateway.getProvider(), gateway);
        }
    }

    public PaymentGateway getGateway(PaymentProvider provider) {
        PaymentGateway gateway = gatewayMap.get(provider);
        if (gateway == null) {
            throw new IllegalArgumentException("Unsupported payment provider: " + provider);
        }
        return gateway;
    }
}
