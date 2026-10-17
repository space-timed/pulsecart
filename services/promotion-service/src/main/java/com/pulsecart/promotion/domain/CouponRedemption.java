package com.pulsecart.promotion.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupon_redemptions", schema = "pulsecart_promotion")
public class CouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "discount_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountApplied;

    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private Instant redeemedAt = Instant.now();

    public CouponRedemption() {}

    public CouponRedemption(Coupon coupon, String orderId, String customerId, BigDecimal discountApplied) {
        this.coupon = coupon;
        this.orderId = orderId;
        this.customerId = customerId;
        this.discountApplied = discountApplied;
        this.redeemedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public BigDecimal getDiscountApplied() { return discountApplied; }
    public void setDiscountApplied(BigDecimal discountApplied) { this.discountApplied = discountApplied; }
    public Instant getRedeemedAt() { return redeemedAt; }
}
