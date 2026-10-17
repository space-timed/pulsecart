package com.pulsecart.pricing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "old_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal newPrice;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PriceHistory() {}

    public PriceHistory(String productId, BigDecimal oldPrice, BigDecimal newPrice, String currency, String changedBy, String reason) {
        this.id = UUID.randomUUID().toString();
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.currency = currency != null ? currency : "USD";
        this.changedBy = changedBy;
        this.reason = reason;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getProductId() { return productId; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public String getCurrency() { return currency; }
    public String getChangedBy() { return changedBy; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
