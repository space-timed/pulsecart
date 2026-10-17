package com.pulsecart.analytics.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "hourly_sales_aggregates", schema = "pulsecart_analytics")
public class HourlySalesAggregate {

    @Id
    @Column(name = "hour_bucket", length = 30)
    private String hourBucket;

    @Column(name = "total_orders", nullable = false)
    private int totalOrders = 0;

    @Column(name = "total_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_items_sold", nullable = false)
    private int totalItemsSold = 0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public HourlySalesAggregate() {}

    public HourlySalesAggregate(String hourBucket, int totalOrders, BigDecimal totalRevenue, int totalItemsSold) {
        this.hourBucket = hourBucket;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalItemsSold = totalItemsSold;
        this.updatedAt = Instant.now();
    }

    public void addSale(BigDecimal amount, int itemsCount) {
        this.totalOrders += 1;
        this.totalRevenue = this.totalRevenue.add(amount != null ? amount : BigDecimal.ZERO);
        this.totalItemsSold += itemsCount;
        this.updatedAt = Instant.now();
    }

    public String getHourBucket() { return hourBucket; }
    public void setHourBucket(String hourBucket) { this.hourBucket = hourBucket; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public int getTotalItemsSold() { return totalItemsSold; }
    public void setTotalItemsSold(int totalItemsSold) { this.totalItemsSold = totalItemsSold; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
