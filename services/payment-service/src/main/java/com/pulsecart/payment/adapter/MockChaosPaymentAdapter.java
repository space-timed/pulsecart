package com.pulsecart.payment.adapter;

import com.pulsecart.payment.domain.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockChaosPaymentAdapter implements PaymentGateway {

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.CHAOS_MOCK;
    }

    @Override
    public PaymentGatewayResult authorize(PaymentGatewayCommand command) {
        String chaos = command.chaosAction();
        if ("FAIL_PAYMENT".equalsIgnoreCase(chaos) || "FAIL_INSUFFICIENT_FUNDS".equalsIgnoreCase(chaos)) {
            return PaymentGatewayResult.failure("CHAOS_INSUFFICIENT_FUNDS", "Simulated card decline: Insufficient balance");
        }
        if ("FAIL_FRAUD_ALERT".equalsIgnoreCase(chaos)) {
            return PaymentGatewayResult.failure("CHAOS_FRAUD_SUSPECTED", "Simulated security decline: High-risk transaction detected");
        }
        if ("FAIL_NETWORK".equalsIgnoreCase(chaos)) {
            return PaymentGatewayResult.failure("CHAOS_GATEWAY_UNAVAILABLE", "Simulated 503: Payment gateway unavailable");
        }

        String txRef = "chaos_auth_" + UUID.randomUUID().toString().substring(0, 8);
        return PaymentGatewayResult.success(txRef, "CHAOS_OK");
    }

    @Override
    public PaymentGatewayResult capture(PaymentGatewayCommand command) {
        String chaos = command.chaosAction();
        if ("FAIL_CAPTURE".equalsIgnoreCase(chaos)) {
            return PaymentGatewayResult.failure("CHAOS_CAPTURE_FAILED", "Simulated capture failure");
        }
        String txRef = "chaos_cap_" + UUID.randomUUID().toString().substring(0, 8);
        return PaymentGatewayResult.success(txRef, "CHAOS_CAPTURED");
    }

    @Override
    public PaymentGatewayResult refund(PaymentGatewayCommand command) {
        String txRef = "chaos_ref_" + UUID.randomUUID().toString().substring(0, 8);
        return PaymentGatewayResult.success(txRef, "CHAOS_REFUNDED");
    }
}
