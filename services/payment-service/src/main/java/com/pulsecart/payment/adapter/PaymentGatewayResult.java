package com.pulsecart.payment.adapter;

public record PaymentGatewayResult(
        boolean success,
        String transactionReference,
        String authorizationCode,
        String errorMessage,
        String errorCode
) {
    public static PaymentGatewayResult success(String txRef, String authCode) {
        return new PaymentGatewayResult(true, txRef, authCode, null, null);
    }

    public static PaymentGatewayResult failure(String errorCode, String errorMessage) {
        return new PaymentGatewayResult(false, null, null, errorMessage, errorCode);
    }
}
