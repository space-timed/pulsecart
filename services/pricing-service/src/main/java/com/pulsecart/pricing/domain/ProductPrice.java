package com.pulsecart.pricing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_prices")
public class ProductPrice {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "product_id", length = 36, nullable = false, unique = true)
    private String productId;

    @Column(name = "base_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal basePrice;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProductPrice() {}

    public ProductPrice(String productId, BigDecimal basePrice, String currency) {
        this.id = UUID.randomUUID().toString();
        this.productId = productId;
        this.basePrice = basePrice;
        this.currency = currency != null ? currency : "USD";
        this.isActive = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updatePrice(BigDecimal newPrice) {
        this.basePrice = newPrice;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public BigDecimal getBasePrice() { return basePrice; }
    public String getCurrency() { return currency; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
