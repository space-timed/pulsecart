package com.pulsecart.fulfillment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fulfillments", schema = "pulsecart_fulfillment")
public class Fulfillment {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "warehouse_id", nullable = false, length = 50)
    private String warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FulfillmentStatus status;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @OneToMany(mappedBy = "fulfillment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FulfillmentItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Fulfillment() {}

    public Fulfillment(String id, String orderId, String customerId, String warehouseId, String shippingAddress) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.warehouseId = warehouseId;
        this.status = FulfillmentStatus.PENDING;
        this.shippingAddress = shippingAddress;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addItem(FulfillmentItem item) {
        items.add(item);
        item.setFulfillment(this);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public FulfillmentStatus getStatus() { return status; }
    public void setStatus(FulfillmentStatus status) { this.status = status; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public List<FulfillmentItem> getItems() { return items; }
    public void setItems(List<FulfillmentItem> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
