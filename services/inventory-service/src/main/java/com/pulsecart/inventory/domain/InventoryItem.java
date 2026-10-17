package com.pulsecart.inventory.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "product_id", length = 36, nullable = false, unique = true)
    private String productId;

    @Column(name = "sku", length = 64, nullable = false, unique = true)
    private String sku;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "total_quantity", nullable = false)
    private int totalQuantity;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public InventoryItem() {}

    public InventoryItem(String productId, String sku, int totalQuantity) {
        this.id = UUID.randomUUID().toString();
        this.productId = productId;
        this.sku = sku;
        this.availableQuantity = totalQuantity;
        this.reservedQuantity = 0;
        this.totalQuantity = totalQuantity;
        this.version = null; // null signals a new transient entity for EntityManager.persist
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void restock(int additionalQuantity) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }
        this.availableQuantity += additionalQuantity;
        this.totalQuantity += additionalQuantity;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public String getSku() { return sku; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
    public int getTotalQuantity() { return totalQuantity; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
