package com.pulsecart.catalog.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "sku", length = 64, nullable = false, unique = true)
    private String sku;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "slug", length = 280, nullable = false, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "base_price", precision = 12, scale = 4, nullable = false)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private ProductStatus status;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "tags")
    private String tags;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductSpecification> specifications = new ArrayList<>();

    public Product() {}

    public Product(String sku, String name, String slug, String description, Category category, BigDecimal basePrice, String imageUrl, String tags) {
        this.id = UUID.randomUUID().toString();
        this.sku = sku;
        this.name = name;
        this.slug = slug != null && !slug.isBlank() ? slug : name.toLowerCase().replaceAll("[^a-z0-9]+", "-") + "-" + UUID.randomUUID().toString().substring(0, 6);
        this.description = description;
        this.category = category;
        this.basePrice = basePrice;
        this.status = ProductStatus.ACTIVE;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String name, String description, BigDecimal basePrice, ProductStatus status, String imageUrl, String tags) {
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.status = status;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.updatedAt = Instant.now();
    }

    public void addSpecification(String key, String value) {
        specifications.add(new ProductSpecification(this, key, value));
    }

    public String getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public BigDecimal getBasePrice() { return basePrice; }
    public ProductStatus getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public String getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<ProductSpecification> getSpecifications() { return specifications; }
}
