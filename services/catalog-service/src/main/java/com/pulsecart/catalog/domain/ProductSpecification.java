package com.pulsecart.catalog.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_specifications")
public class ProductSpecification {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "spec_key", length = 100, nullable = false)
    private String specKey;

    @Column(name = "spec_value", length = 255, nullable = false)
    private String specValue;

    public ProductSpecification() {}

    public ProductSpecification(Product product, String specKey, String specValue) {
        this.id = UUID.randomUUID().toString();
        this.product = product;
        this.specKey = specKey;
        this.specValue = specValue;
    }

    public String getId() { return id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getSpecKey() { return specKey; }
    public String getSpecValue() { return specValue; }
}
