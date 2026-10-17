package com.pulsecart.payment.adapter;

import com.pulsecart.payment.domain.PaymentProvider;

public interface PaymentGateway {
    PaymentProvider getProvider();
    PaymentGatewayResult authorize(PaymentGatewayCommand command);
    PaymentGatewayResult capture(PaymentGatewayCommand command);
    PaymentGatewayResult refund(PaymentGatewayCommand command);
}
