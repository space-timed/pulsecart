package com.pulsecart.returns.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "returns", schema = "pulsecart_returns")
public class ReturnRequest {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnStatus status;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ReturnItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ReturnRequest() {}

    public ReturnRequest(String id, String orderId, String customerId, ReturnStatus status, String reason, BigDecimal refundAmount) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = status;
        this.reason = reason;
        this.refundAmount = refundAmount != null ? refundAmount : BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addItem(ReturnItem item) {
        items.add(item);
        item.setReturnRequest(this);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public ReturnStatus getStatus() { return status; }
    public void setStatus(ReturnStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public List<ReturnItem> getItems() { return items; }
    public void setItems(List<ReturnItem> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
