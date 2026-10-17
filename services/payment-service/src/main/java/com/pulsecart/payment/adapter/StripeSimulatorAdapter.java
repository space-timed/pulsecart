package com.pulsecart.payment.adapter;

import com.pulsecart.payment.domain.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StripeSimulatorAdapter implements PaymentGateway {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public PaymentGatewayResult authorize(PaymentGatewayCommand command) {
        if ("FAIL_INSUFFICIENT_FUNDS".equalsIgnoreCase(command.chaosAction())) {
            return PaymentGatewayResult.failure("card_declined", "Your card has insufficient funds.");
        }
        if ("FAIL_CARD_EXPIRED".equalsIgnoreCase(command.chaosAction())) {
            return PaymentGatewayResult.failure("expired_card", "Your card has expired.");
        }

        String txRef = "ch_stripe_" + UUID.randomUUID().toString().substring(0, 12);
        String authCode = "AUTH_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentGatewayResult.success(txRef, authCode);
    }

    @Override
    public PaymentGatewayResult capture(PaymentGatewayCommand command) {
        String txRef = "cap_stripe_" + UUID.randomUUID().toString().substring(0, 12);
        return PaymentGatewayResult.success(txRef, "CAPTURED");
    }

    @Override
    public PaymentGatewayResult refund(PaymentGatewayCommand command) {
        String txRef = "re_stripe_" + UUID.randomUUID().toString().substring(0, 12);
        return PaymentGatewayResult.success(txRef, "REFUNDED");
    }
}
