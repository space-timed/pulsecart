package com.pulsecart.shipping.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shipments", schema = "pulsecart_shipping")
public class Shipment {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShippingCarrier carrier;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 100)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(name = "destination_address", columnDefinition = "TEXT")
    private String destinationAddress;

    @Column(name = "estimated_delivery_at", nullable = false)
    private Instant estimatedDeliveryAt;

    @Column(name = "dispatched_at", nullable = false)
    private Instant dispatchedAt = Instant.now();

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Shipment() {}

    public Shipment(String id, String orderId, String customerId, ShippingCarrier carrier,
                    String trackingNumber, String destinationAddress, Instant estimatedDeliveryAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = ShipmentStatus.DISPATCHED;
        this.destinationAddress = destinationAddress;
        this.estimatedDeliveryAt = estimatedDeliveryAt;
        this.dispatchedAt = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public ShippingCarrier getCarrier() { return carrier; }
    public void setCarrier(ShippingCarrier carrier) { this.carrier = carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }
    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
    public Instant getEstimatedDeliveryAt() { return estimatedDeliveryAt; }
    public void setEstimatedDeliveryAt(Instant estimatedDeliveryAt) { this.estimatedDeliveryAt = estimatedDeliveryAt; }
    public Instant getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(Instant dispatchedAt) { this.dispatchedAt = dispatchedAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
