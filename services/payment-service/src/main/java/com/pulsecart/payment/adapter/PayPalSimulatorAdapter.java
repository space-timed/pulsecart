package com.pulsecart.payment.adapter;

import com.pulsecart.payment.domain.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PayPalSimulatorAdapter implements PaymentGateway {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.PAYPAL;
    }

    @Override
    public PaymentGatewayResult authorize(PaymentGatewayCommand command) {
        if ("FAIL_WALLET_EMPTY".equalsIgnoreCase(command.chaosAction())) {
            return PaymentGatewayResult.failure("paypal_balance_insufficient", "PayPal account balance is insufficient.");
        }
        String txRef = "PAYPAL-AUTH-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        return PaymentGatewayResult.success(txRef, "PP_APPROVED");
    }

    @Override
    public PaymentGatewayResult capture(PaymentGatewayCommand command) {
        String txRef = "PAYPAL-CAP-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        return PaymentGatewayResult.success(txRef, "PP_CAPTURED");
    }

    @Override
    public PaymentGatewayResult refund(PaymentGatewayCommand command) {
        String txRef = "PAYPAL-REF-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        return PaymentGatewayResult.success(txRef, "PP_REFUNDED");
    }
}
