package com.pulsecart.order.client;

import java.math.BigDecimal;

public interface PaymentClient {

    record PaymentAuthResult(boolean success, String paymentId, String authorizationCode, String errorMessage) {}

    PaymentAuthResult authorizePayment(String orderId, String customerId, BigDecimal amount,
                                       String paymentMethodToken, String chaosAction);
}
