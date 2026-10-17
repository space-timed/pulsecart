package com.pulsecart.fulfillment.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "fulfillment_items", schema = "pulsecart_fulfillment")
public class FulfillmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fulfillment_id", nullable = false)
    private Fulfillment fulfillment;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    public FulfillmentItem() {}

    public FulfillmentItem(String productId, String sku, String productName, int quantity) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Fulfillment getFulfillment() { return fulfillment; }
    public void setFulfillment(Fulfillment fulfillment) { this.fulfillment = fulfillment; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
