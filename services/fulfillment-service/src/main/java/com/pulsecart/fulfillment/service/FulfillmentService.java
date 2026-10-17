package com.pulsecart.fulfillment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.EventEnvelope;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.fulfillment.domain.*;
import com.pulsecart.fulfillment.dto.FulfillmentDtos.*;
import com.pulsecart.fulfillment.repository.FulfillmentItemRepository;
import com.pulsecart.fulfillment.repository.FulfillmentOutboxEventRepository;
import com.pulsecart.fulfillment.repository.FulfillmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class FulfillmentService {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentService.class);

    private final FulfillmentRepository fulfillmentRepository;
    private final FulfillmentItemRepository itemRepository;
    private final FulfillmentOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public FulfillmentService(FulfillmentRepository fulfillmentRepository,
                              FulfillmentItemRepository itemRepository,
                              FulfillmentOutboxEventRepository outboxEventRepository,
                              ObjectMapper objectMapper) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.itemRepository = itemRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public FulfillmentResponse processFulfillment(ProcessFulfillmentRequest req) {
        // Idempotency: Return existing fulfillment if already processed
        Optional<Fulfillment> existingOpt = fulfillmentRepository.findByOrderId(req.orderId());
        if (existingOpt.isPresent()) {
            log.info("Idempotent fulfillment check: Order {} already fulfilled", req.orderId());
            return toResponse(existingOpt.get());
        }

        String fulfillmentId = "ful_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String warehouseId = Math.abs(req.orderId().hashCode()) % 2 == 0 ? "WH-EAST-1" : "WH-WEST-1";

        Fulfillment fulfillment = new Fulfillment(fulfillmentId, req.orderId(), req.customerId(), warehouseId, req.shippingAddress());
        for (FulfillmentItemRequest itemReq : req.items()) {
            fulfillment.addItem(new FulfillmentItem(itemReq.productId(), itemReq.sku(), itemReq.productName(), itemReq.quantity()));
        }

        // Simulate warehouse picking and packing
        fulfillment.setStatus(FulfillmentStatus.FULFILLED);
        fulfillment.setUpdatedAt(Instant.now());
        Fulfillment saved = fulfillmentRepository.save(fulfillment);

        // Transactional Outbox Event for pulsecart.fulfillment.events
        Map<String, Object> payload = Map.of(
                "fulfillmentId", saved.getId(),
                "orderId", saved.getOrderId(),
                "customerId", saved.getCustomerId(),
                "warehouseId", saved.getWarehouseId(),
                "status", saved.getStatus().name(),
                "fulfilledAt", Instant.now().toString()
        );

        EventEnvelope<Map<String, Object>> envelope = EventEnvelope.create(
                "fulfillment.completed",
                1,
                saved.getId(),
                "Fulfillment",
                null,
                null,
                "fulfillment-service",
                payload
        );

        try {
            String envelopeJson = objectMapper.writeValueAsString(envelope);
            FulfillmentOutboxEvent outboxEvent = new FulfillmentOutboxEvent(
                    "Fulfillment", saved.getId(), "FulfillmentCompleted", PulseCartTopics.FULFILLMENT_EVENTS, envelopeJson
            );
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize fulfillment outbox event: {}", e.getMessage());
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Optional<FulfillmentResponse> getFulfillment(String id) {
        return fulfillmentRepository.findById(id).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<FulfillmentResponse> getFulfillmentByOrderId(String orderId) {
        return fulfillmentRepository.findByOrderId(orderId).map(this::toResponse);
    }

    private FulfillmentResponse toResponse(Fulfillment f) {
        List<FulfillmentItemDto> itemDtos = f.getItems().stream()
                .map(i -> new FulfillmentItemDto(i.getProductId(), i.getSku(), i.getProductName(), i.getQuantity()))
                .toList();

        return new FulfillmentResponse(
                f.getId(),
                f.getOrderId(),
                f.getCustomerId(),
                f.getWarehouseId(),
                f.getStatus(),
                f.getShippingAddress(),
                itemDtos,
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
