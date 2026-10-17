package com.pulsecart.catalog.repository;

import com.pulsecart.catalog.domain.Product;
import com.pulsecart.catalog.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findBySlug(String slug);
    Page<Product> findByCategoryId(String categoryId, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    boolean existsBySku(String sku);
}
