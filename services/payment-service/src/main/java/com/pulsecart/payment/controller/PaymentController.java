package com.pulsecart.payment.controller;

import com.pulsecart.payment.dto.PaymentDtos.*;
import com.pulsecart.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizePaymentResponse> authorize(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Chaos-Action", required = false) String headerChaosAction,
            @Valid @RequestBody AuthorizePaymentRequest request) {

        String effectiveChaos = request.chaosAction() != null ? request.chaosAction() : headerChaosAction;
        AuthorizePaymentRequest effectiveReq = new AuthorizePaymentRequest(
                request.orderId(),
                request.customerId(),
                request.amount(),
                request.currency(),
                request.provider(),
                request.paymentMethodToken(),
                effectiveChaos
        );

        AuthorizePaymentResponse response = paymentService.authorizePayment(effectiveReq, idempotencyKey);
        if (response.status() == com.pulsecart.payment.domain.PaymentStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<CapturePaymentResponse> capture(@PathVariable String paymentId) {
        CapturePaymentResponse response = paymentService.capturePayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<RefundPaymentResponse> refund(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundPaymentRequest request) {
        RefundPaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponse> getPayment(@PathVariable String paymentId) {
        return paymentService.getPayment(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDetailResponse> getPaymentByOrderId(@PathVariable String orderId) {
        return paymentService.getPaymentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
