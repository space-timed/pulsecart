package com.pulsecart.returns.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.returns.domain.ReturnItem;
import com.pulsecart.returns.domain.ReturnRequest;
import com.pulsecart.returns.domain.ReturnStatus;
import com.pulsecart.returns.domain.ReturnsOutboxEvent;
import com.pulsecart.returns.dto.ReturnsDtos.*;
import com.pulsecart.returns.repository.ReturnRequestRepository;
import com.pulsecart.returns.repository.ReturnsOutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class ReturnsService {

    private static final Logger log = LoggerFactory.getLogger(ReturnsService.class);
    private static final long RETURN_WINDOW_DAYS = 30;

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnsOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ReturnsService(ReturnRequestRepository returnRequestRepository,
                          ReturnsOutboxEventRepository outboxEventRepository,
                          ObjectMapper objectMapper) {
        this.returnRequestRepository = returnRequestRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ReturnResponse createReturnRequest(CreateReturnRequest request) {
        Instant now = Instant.now();
        long daysSinceDelivery = Duration.between(request.deliveryDate(), now).toDays();

        ReturnStatus status;
        String reasonNote;

        if (daysSinceDelivery > RETURN_WINDOW_DAYS) {
            status = ReturnStatus.REJECTED;
            reasonNote = request.reason() + " [REJECTED: Exceeded " + RETURN_WINDOW_DAYS + "-day return window (" + daysSinceDelivery + " days elapsed)]";
            log.warn("Return rejected for order {}: exceeded 30-day window ({} days)", request.orderId(), daysSinceDelivery);
        } else {
            status = ReturnStatus.APPROVED;
            reasonNote = request.reason();
            log.info("Return approved for order {}: within return window ({} days elapsed)", request.orderId(), daysSinceDelivery);
        }

        BigDecimal totalRefund = BigDecimal.ZERO;
        for (ReturnItemRequest itemReq : request.items()) {
            BigDecimal lineTotal = itemReq.price().multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalRefund = totalRefund.add(lineTotal);
        }

        String rmaId = "RMA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ReturnRequest returnRequest = new ReturnRequest(
                rmaId,
                request.orderId(),
                request.customerId(),
                status,
                reasonNote,
                totalRefund
        );

        for (ReturnItemRequest itemReq : request.items()) {
            ReturnItem item = new ReturnItem(
                    returnRequest,
                    itemReq.productId(),
                    itemReq.quantity(),
                    itemReq.condition(),
                    itemReq.price()
            );
            returnRequest.addItem(item);
        }

        ReturnRequest saved = returnRequestRepository.save(returnRequest);

        // Record Transactional Outbox Event
        String eventType = (status == ReturnStatus.APPROVED) ? "ReturnApproved" : "ReturnRejected";
        saveOutboxEvent(saved, eventType);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReturnResponse getReturnById(String rmaId) {
        return returnRequestRepository.findById(rmaId)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Return RMA not found: " + rmaId));
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getReturnsByOrderId(String orderId) {
        return returnRequestRepository.findByOrderId(orderId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReturnResponse receiveReturn(String rmaId, ProcessReturnReceiptRequest request) {
        ReturnRequest returnRequest = returnRequestRepository.findById(rmaId)
                .orElseThrow(() -> new NoSuchElementException("Return RMA not found: " + rmaId));

        if (returnRequest.getStatus() == ReturnStatus.REJECTED) {
            throw new IllegalStateException("Cannot receive rejected RMA: " + rmaId);
        }

        returnRequest.setStatus(ReturnStatus.RECEIVED);
        ReturnRequest saved = returnRequestRepository.save(returnRequest);

        log.info("RMA {} received at warehouse for restocking/refund processing", rmaId);
        saveOutboxEvent(saved, "ReturnReceived");

        return toResponse(saved);
    }

    private void saveOutboxEvent(ReturnRequest rma, String eventType) {
        try {
            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("rmaId", rma.getId());
            payloadMap.put("orderId", rma.getOrderId());
            payloadMap.put("customerId", rma.getCustomerId());
            payloadMap.put("status", rma.getStatus().name());
            payloadMap.put("refundAmount", rma.getRefundAmount());
            payloadMap.put("timestamp", Instant.now().toString());

            String payload = objectMapper.writeValueAsString(payloadMap);
            ReturnsOutboxEvent outboxEvent = new ReturnsOutboxEvent(
                    UUID.randomUUID().toString(),
                    "ReturnRequest",
                    rma.getId(),
                    eventType,
                    payload
            );
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed serializing returns outbox event for RMA {}: {}", rma.getId(), e.getMessage());
        }
    }

    private ReturnResponse toResponse(ReturnRequest r) {
        List<ReturnItemResponse> itemResponses = r.getItems().stream()
                .map(i -> new ReturnItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getQuantity(),
                        i.getCondition(),
                        i.getPrice()
                ))
                .toList();

        return new ReturnResponse(
                r.getId(),
                r.getOrderId(),
                r.getCustomerId(),
                r.getStatus(),
                r.getReason(),
                r.getRefundAmount(),
                itemResponses,
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
