package com.pulsecart.payment.adapter;

import java.math.BigDecimal;

public record PaymentGatewayCommand(
        String paymentId,
        String orderId,
        BigDecimal amount,
        String currency,
        String paymentMethodToken,
        String chaosAction
) {}
