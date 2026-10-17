package com.pulsecart.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.payment.adapter.*;
import com.pulsecart.payment.domain.*;
import com.pulsecart.payment.dto.PaymentDtos.*;
import com.pulsecart.payment.repository.PaymentIdempotencyRepository;
import com.pulsecart.payment.repository.PaymentRepository;
import com.pulsecart.payment.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentIdempotencyRepository idempotencyRepository;
    private final PaymentGatewayFactory gatewayFactory;
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentTransactionRepository transactionRepository,
                          PaymentIdempotencyRepository idempotencyRepository,
                          PaymentGatewayFactory gatewayFactory,
                          ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.gatewayFactory = gatewayFactory;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuthorizePaymentResponse authorizePayment(AuthorizePaymentRequest req, String idempotencyKey) {
        String requestHash = computeHash(req);

        // 1. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<PaymentIdempotencyRecord> existingOpt = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOpt.isPresent()) {
                PaymentIdempotencyRecord record = existingOpt.get();
                if ("COMPLETED".equalsIgnoreCase(record.getStatus()) && record.getResponseBody() != null) {
                    try {
                        return objectMapper.readValue(record.getResponseBody(), AuthorizePaymentResponse.class);
                    } catch (JsonProcessingException ignored) {
                    }
                }
            }
        }

        // 2. Resolve Gateway
        PaymentProvider provider = req.provider() != null ? req.provider() : PaymentProvider.STRIPE;
        PaymentGateway gateway = gatewayFactory.getGateway(provider);

        String paymentId = "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        Payment payment = new Payment(paymentId, req.orderId(), req.customerId(), req.amount(), req.currency(), provider);

        PaymentGatewayCommand cmd = new PaymentGatewayCommand(
                paymentId,
                req.orderId(),
                req.amount(),
                req.currency() != null ? req.currency() : "USD",
                req.paymentMethodToken(),
                req.chaosAction()
        );

        PaymentGatewayResult result = gateway.authorize(cmd);

        AuthorizePaymentResponse response;
        if (result.success()) {
            payment.setStatus(PaymentStatus.AUTHORIZED);
            payment.setAuthorizationCode(result.authorizationCode());
            PaymentTransaction tx = new PaymentTransaction(payment, TransactionType.AUTHORIZE, req.amount(),
                    "SUCCESS", result.transactionReference(), null);
            payment.addTransaction(tx);
            paymentRepository.save(payment);

            response = new AuthorizePaymentResponse(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getStatus(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getAuthorizationCode(),
                    result.transactionReference(),
                    null
            );
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.errorMessage());
            PaymentTransaction tx = new PaymentTransaction(payment, TransactionType.AUTHORIZE, req.amount(),
                    "FAILED", null, result.errorMessage());
            payment.addTransaction(tx);
            paymentRepository.save(payment);

            response = new AuthorizePaymentResponse(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getStatus(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    null,
                    null,
                    result.errorMessage()
            );
        }

        // 3. Persist Idempotency State
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            try {
                String responseJson = objectMapper.writeValueAsString(response);
                PaymentIdempotencyRecord record = new PaymentIdempotencyRecord(idempotencyKey, requestHash, paymentId, "COMPLETED");
                record.setResponseBody(responseJson);
                idempotencyRepository.save(record);
            } catch (JsonProcessingException ignored) {
            }
        }

        return response;
    }

    @Transactional
    public CapturePaymentResponse capturePayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new IllegalStateException("Cannot capture payment with status: " + payment.getStatus());
        }

        PaymentGateway gateway = gatewayFactory.getGateway(payment.getProvider());
        PaymentGatewayCommand cmd = new PaymentGatewayCommand(paymentId, payment.getOrderId(), payment.getAmount(),
                payment.getCurrency(), null, null);

        PaymentGatewayResult result = gateway.capture(cmd);
        if (result.success()) {
            payment.setStatus(PaymentStatus.CAPTURED);
            payment.setUpdatedAt(Instant.now());
            PaymentTransaction tx = new PaymentTransaction(payment, TransactionType.CAPTURE, payment.getAmount(),
                    "SUCCESS", result.transactionReference(), null);
            payment.addTransaction(tx);
            paymentRepository.save(payment);

            return new CapturePaymentResponse(payment.getId(), payment.getOrderId(), payment.getStatus(),
                    payment.getAmount(), result.transactionReference(), "Payment captured successfully");
        } else {
            payment.setFailureReason(result.errorMessage());
            PaymentTransaction tx = new PaymentTransaction(payment, TransactionType.CAPTURE, payment.getAmount(),
                    "FAILED", null, result.errorMessage());
            payment.addTransaction(tx);
            paymentRepository.save(payment);

            return new CapturePaymentResponse(payment.getId(), payment.getOrderId(), payment.getStatus(),
                    payment.getAmount(), null, "Capture failed: " + result.errorMessage());
        }
    }

    @Transactional
    public RefundPaymentResponse refundPayment(String paymentId, RefundPaymentRequest req) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Only CAPTURED payments can be refunded. Current status: " + payment.getStatus());
        }

        PaymentGateway gateway = gatewayFactory.getGateway(payment.getProvider());
        PaymentGatewayCommand cmd = new PaymentGatewayCommand(paymentId, payment.getOrderId(), req.amount(),
                payment.getCurrency(), null, null);

        PaymentGatewayResult result = gateway.refund(cmd);
        if (result.success()) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(Instant.now());
            PaymentTransaction tx = new PaymentTransaction(payment, TransactionType.REFUND, req.amount(),
                    "SUCCESS", result.transactionReference(), null);
            payment.addTransaction(tx);
            paymentRepository.save(payment);

            return new RefundPaymentResponse(payment.getId(), payment.getOrderId(), payment.getStatus(),
                    req.amount(), result.transactionReference(), "Refund processed successfully");
        } else {
            return new RefundPaymentResponse(payment.getId(), payment.getOrderId(), payment.getStatus(),
                    BigDecimal.ZERO, null, "Refund failed: " + result.errorMessage());
        }
    }

    public Optional<PaymentDetailResponse> getPayment(String paymentId) {
        return paymentRepository.findById(paymentId).map(this::toDetailResponse);
    }

    public Optional<PaymentDetailResponse> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId).map(this::toDetailResponse);
    }

    private PaymentDetailResponse toDetailResponse(Payment p) {
        List<PaymentTransactionDto> txDtos = p.getTransactions().stream()
                .map(t -> new PaymentTransactionDto(
                        t.getId(),
                        t.getType(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getGatewayReference(),
                        t.getErrorMessage(),
                        t.getCreatedAt()
                ))
                .toList();

        return new PaymentDetailResponse(
                p.getId(),
                p.getOrderId(),
                p.getCustomerId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getProvider(),
                p.getAuthorizationCode(),
                p.getFailureReason(),
                txDtos,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private String computeHash(AuthorizePaymentRequest req) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = req.orderId() + ":" + req.customerId() + ":" + req.amount();
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(req.hashCode());
        }
    }
}
