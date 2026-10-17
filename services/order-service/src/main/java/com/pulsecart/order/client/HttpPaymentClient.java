package com.pulsecart.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpPaymentClient implements PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(HttpPaymentClient.class);
    private final RestClient restClient;

    public HttpPaymentClient(@Value("${pulsecart.services.payment-url:http://localhost:8086}") String paymentUrl) {
        this.restClient = RestClient.builder().baseUrl(paymentUrl).build();
    }

    @Override
    public PaymentAuthResult authorizePayment(String orderId, String customerId, BigDecimal amount,
                                              String paymentMethodToken, String chaosAction) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("orderId", orderId);
            body.put("customerId", customerId);
            body.put("amount", amount);
            body.put("currency", "USD");
            body.put("paymentMethodToken", paymentMethodToken != null ? paymentMethodToken : "tok_default");
            if (chaosAction != null && !chaosAction.isBlank()) {
                body.put("chaosAction", chaosAction);
            }

            var requestSpec = restClient.post()
                    .uri("/api/v1/payments/authorize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

            if (chaosAction != null && !chaosAction.isBlank()) {
                requestSpec.header("X-Chaos-Action", chaosAction);
            }

            ResponseEntity<Map> response = requestSpec
                    .retrieve()
                    .onStatus(status -> status.value() == 402, (req, resp) -> {
                        // Payment decline returns 402, handled gracefully below
                    })
                    .toEntity(Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String paymentId = (String) response.getBody().get("paymentId");
                String authCode = (String) response.getBody().get("authorizationCode");
                return new PaymentAuthResult(true, paymentId, authCode, null);
            } else if (response.getBody() != null) {
                String errMsg = (String) response.getBody().get("errorMessage");
                return new PaymentAuthResult(false, null, null, errMsg != null ? errMsg : "Payment declined");
            }
            return new PaymentAuthResult(false, null, null, "Payment declined with HTTP status " + response.getStatusCode());
        } catch (Exception e) {
            log.warn("Payment authorization call failed: {}", e.getMessage());
            return new PaymentAuthResult(false, null, null, e.getMessage());
        }
    }
}
