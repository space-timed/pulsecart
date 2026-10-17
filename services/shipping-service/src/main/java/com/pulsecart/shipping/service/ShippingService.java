package com.pulsecart.shipping.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.EventEnvelope;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.shipping.adapter.CarrierAdapter;
import com.pulsecart.shipping.adapter.CarrierAdapterFactory;
import com.pulsecart.shipping.adapter.CarrierDispatchCommand;
import com.pulsecart.shipping.adapter.CarrierDispatchResult;
import com.pulsecart.shipping.domain.Shipment;
import com.pulsecart.shipping.domain.ShippingCarrier;
import com.pulsecart.shipping.domain.ShippingOutboxEvent;
import com.pulsecart.shipping.dto.ShippingDtos.*;
import com.pulsecart.shipping.repository.ShipmentRepository;
import com.pulsecart.shipping.repository.ShippingOutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

    private final ShipmentRepository shipmentRepository;
    private final ShippingOutboxEventRepository outboxEventRepository;
    private final CarrierAdapterFactory carrierFactory;
    private final ObjectMapper objectMapper;

    public ShippingService(ShipmentRepository shipmentRepository,
                           ShippingOutboxEventRepository outboxEventRepository,
                           CarrierAdapterFactory carrierFactory,
                           ObjectMapper objectMapper) {
        this.shipmentRepository = shipmentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.carrierFactory = carrierFactory;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ShipmentResponse dispatchShipment(DispatchShipmentRequest req) {
        // Idempotency check
        Optional<Shipment> existingOpt = shipmentRepository.findByOrderId(req.orderId());
        if (existingOpt.isPresent()) {
            log.info("Idempotent shipping check: Order {} already dispatched", req.orderId());
            return toResponse(existingOpt.get());
        }

        ShippingCarrier carrier = req.carrier() != null ? req.carrier() : ShippingCarrier.FEDEX;
        CarrierAdapter adapter = carrierFactory.getAdapter(carrier);

        String shipmentId = "ship_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        CarrierDispatchCommand cmd = new CarrierDispatchCommand(shipmentId, req.orderId(), req.destinationAddress());
        CarrierDispatchResult result = adapter.dispatch(cmd);

        Shipment shipment = new Shipment(
                shipmentId,
                req.orderId(),
                req.customerId(),
                carrier,
                result.trackingNumber(),
                req.destinationAddress(),
                result.estimatedDeliveryAt()
        );

        Shipment saved = shipmentRepository.save(shipment);

        // Transactional Outbox Event for pulsecart.shipping.events
        Map<String, Object> payload = Map.of(
                "shipmentId", saved.getId(),
                "orderId", saved.getOrderId(),
                "customerId", saved.getCustomerId(),
                "carrier", saved.getCarrier().name(),
                "trackingNumber", saved.getTrackingNumber(),
                "status", saved.getStatus().name(),
                "estimatedDeliveryAt", saved.getEstimatedDeliveryAt().toString(),
                "dispatchedAt", saved.getDispatchedAt().toString()
        );

        EventEnvelope<Map<String, Object>> envelope = EventEnvelope.create(
                "shipping.dispatched",
                1,
                saved.getId(),
                "Shipment",
                null,
                null,
                "shipping-service",
                payload
        );

        try {
            String envelopeJson = objectMapper.writeValueAsString(envelope);
            ShippingOutboxEvent outboxEvent = new ShippingOutboxEvent(
                    "Shipment", saved.getId(), "ShippingDispatched", PulseCartTopics.SHIPPING_EVENTS, envelopeJson
            );
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize shipping outbox event: {}", e.getMessage());
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentResponse> getShipment(String id) {
        return shipmentRepository.findById(id).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentResponse> getShipmentByOrderId(String orderId) {
        return shipmentRepository.findByOrderId(orderId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<ShipmentResponse> getShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber).map(this::toResponse);
    }

    private ShipmentResponse toResponse(Shipment s) {
        return new ShipmentResponse(
                s.getId(),
                s.getOrderId(),
                s.getCustomerId(),
                s.getCarrier(),
                s.getTrackingNumber(),
                s.getStatus(),
                s.getDestinationAddress(),
                s.getEstimatedDeliveryAt(),
                s.getDispatchedAt(),
                s.getDeliveredAt(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
