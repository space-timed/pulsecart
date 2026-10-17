package com.pulsecart.analytics.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "product_sales_velocity", schema = "pulsecart_analytics")
public class ProductSalesVelocity {

    @Id
    @Column(name = "product_id", length = 100)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "units_sold", nullable = false)
    private int unitsSold = 0;

    @Column(name = "total_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "velocity_score", nullable = false, precision = 8, scale = 2)
    private BigDecimal velocityScore = BigDecimal.ZERO;

    @Column(name = "last_order_at", nullable = false)
    private Instant lastOrderAt = Instant.now();

    public ProductSalesVelocity() {}

    public ProductSalesVelocity(String productId, String productName, int unitsSold, BigDecimal totalRevenue) {
        this.productId = productId;
        this.productName = productName != null ? productName : productId;
        this.unitsSold = unitsSold;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.velocityScore = calculateVelocity(unitsSold);
        this.lastOrderAt = Instant.now();
    }

    public void addSale(int quantity, BigDecimal revenue) {
        this.unitsSold += quantity;
        this.totalRevenue = this.totalRevenue.add(revenue != null ? revenue : BigDecimal.ZERO);
        this.velocityScore = calculateVelocity(this.unitsSold);
        this.lastOrderAt = Instant.now();
    }

    private BigDecimal calculateVelocity(int units) {
        // Normalized velocity score: units * 1.5 rounded to 2 decimals
        return BigDecimal.valueOf(units).multiply(BigDecimal.valueOf(1.5)).setScale(2, RoundingMode.HALF_UP);
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getUnitsSold() { return unitsSold; }
    public void setUnitsSold(int unitsSold) { this.unitsSold = unitsSold; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public BigDecimal getVelocityScore() { return velocityScore; }
    public void setVelocityScore(BigDecimal velocityScore) { this.velocityScore = velocityScore; }
    public Instant getLastOrderAt() { return lastOrderAt; }
    public void setLastOrderAt(Instant lastOrderAt) { this.lastOrderAt = lastOrderAt; }
}
