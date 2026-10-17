package com.pulsecart.cart.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {

    private String productId;
    private String sku;
    private String productName;
    private int quantity;
    private BigDecimal estimatedUnitPrice;
    private String imageUrl;

    public CartItem() {}

    public CartItem(String productId, String sku, String productName, int quantity, BigDecimal estimatedUnitPrice, String imageUrl) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.quantity = quantity;
        this.estimatedUnitPrice = estimatedUnitPrice;
        this.imageUrl = imageUrl;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getEstimatedUnitPrice() { return estimatedUnitPrice; }
    public void setEstimatedUnitPrice(BigDecimal estimatedUnitPrice) { this.estimatedUnitPrice = estimatedUnitPrice; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
