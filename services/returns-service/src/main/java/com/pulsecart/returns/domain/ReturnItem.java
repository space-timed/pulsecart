package com.pulsecart.returns.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "return_items", schema = "pulsecart_returns")
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    @JsonIgnore
    private ReturnRequest returnRequest;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 50)
    private String condition = "UNOPENED";

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    public ReturnItem() {}

    public ReturnItem(ReturnRequest returnRequest, String productId, int quantity, String condition, BigDecimal price) {
        this.returnRequest = returnRequest;
        this.productId = productId;
        this.quantity = quantity;
        this.condition = condition != null ? condition : "UNOPENED";
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public ReturnRequest getReturnRequest() { return returnRequest; }
    public void setReturnRequest(ReturnRequest returnRequest) { this.returnRequest = returnRequest; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
