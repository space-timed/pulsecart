package com.pulsecart.search.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_search_index", schema = "pulsecart_search")
public class ProductSearchIndex {

    @Id
    @Column(name = "product_id", length = 100)
    private String productId;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id", length = 100)
    private String categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    private String currency = "USD";

    @Column(name = "in_stock", nullable = false)
    private boolean inStock = true;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 0;

    @Column(length = 255)
    private String tags;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ProductSearchIndex() {}

    public ProductSearchIndex(String productId, String sku, String name, String description,
                              String categoryId, String categoryName, BigDecimal price,
                              String currency, boolean inStock, int stockQuantity, String tags) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "USD";
        this.inStock = inStock;
        this.stockQuantity = stockQuantity;
        this.tags = tags;
        this.updatedAt = Instant.now();
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public boolean isInStock() { return inStock; }
    public void setInStock(boolean inStock) { this.inStock = inStock; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
