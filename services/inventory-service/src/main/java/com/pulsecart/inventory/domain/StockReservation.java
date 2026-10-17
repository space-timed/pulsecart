package com.pulsecart.inventory.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stock_reservations")
public class StockReservation {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "order_id", length = 36, nullable = false)
    private String orderId;

    @Column(name = "status", length = 30, nullable = false)
    private String status; // RESERVED, CONFIRMED, RELEASED, EXPIRED

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReservationItem> items = new ArrayList<>();

    public StockReservation() {}

    public StockReservation(String orderId, Instant expiresAt) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.status = "RESERVED";
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addItem(String productId, int quantity) {
        items.add(new ReservationItem(this, productId, quantity));
    }

    public void markConfirmed() {
        this.status = "CONFIRMED";
        this.updatedAt = Instant.now();
    }

    public void markReleased() {
        this.status = "RELEASED";
        this.updatedAt = Instant.now();
    }

    public void markExpired() {
        this.status = "EXPIRED";
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<ReservationItem> getItems() { return items; }
}
