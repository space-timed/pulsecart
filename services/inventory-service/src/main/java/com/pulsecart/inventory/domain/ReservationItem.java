package com.pulsecart.inventory.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "reservation_items")
public class ReservationItem {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private StockReservation reservation;

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public ReservationItem() {}

    public ReservationItem(StockReservation reservation, String productId, int quantity) {
        this.id = UUID.randomUUID().toString();
        this.reservation = reservation;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public StockReservation getReservation() { return reservation; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}
