package com.pulsecart.search.service;

import com.pulsecart.search.domain.ProductSearchIndex;
import com.pulsecart.search.dto.SearchDtos.*;
import com.pulsecart.search.repository.ProductSearchIndexRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final ProductSearchIndexRepository repository;

    public SearchService(ProductSearchIndexRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SearchResponse search(String query, String category, BigDecimal minPrice, BigDecimal maxPrice,
                                 Boolean inStock, String sortBy) {
        Specification<ProductSearchIndex> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate tagMatch = cb.like(cb.lower(root.get("tags")), pattern);
                predicates.add(cb.or(nameMatch, descMatch, tagMatch));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("categoryName")), category.trim().toLowerCase()));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (inStock != null) {
                predicates.add(cb.equal(root.get("inStock"), inStock));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        }

        List<ProductSearchIndex> items = repository.findAll(spec, sort);
        List<SearchResultDto> results = items.stream().map(this::toDto).toList();

        return new SearchResponse(query != null ? query : "", results.size(), results);
    }

    @Transactional
    public SearchResultDto indexProduct(IndexProductRequest req) {
        ProductSearchIndex entity = repository.findById(req.productId())
                .orElse(new ProductSearchIndex());

        entity.setProductId(req.productId());
        entity.setSku(req.sku() != null ? req.sku() : req.productId());
        entity.setName(req.name() != null ? req.name() : "Unnamed Product");
        entity.setDescription(req.description() != null ? req.description() : "");
        entity.setCategoryId(req.categoryId());
        entity.setCategoryName(req.categoryName() != null ? req.categoryName() : "General");
        if (req.price() != null) {
            entity.setPrice(req.price());
        }
        if (req.currency() != null) {
            entity.setCurrency(req.currency());
        }
        if (req.inStock() != null) {
            entity.setInStock(req.inStock());
        }
        if (req.stockQuantity() != null) {
            entity.setStockQuantity(req.stockQuantity());
        }
        if (req.tags() != null) {
            entity.setTags(req.tags());
        }
        entity.setUpdatedAt(Instant.now());

        ProductSearchIndex saved = repository.save(entity);
        log.info("Indexed product in search read projection: [id: {}, name: {}, price: {}]",
                saved.getProductId(), saved.getName(), saved.getPrice());

        return toDto(saved);
    }

    @Transactional
    public void updatePrice(String productId, BigDecimal newPrice) {
        repository.findById(productId).ifPresent(p -> {
            p.setPrice(newPrice);
            p.setUpdatedAt(Instant.now());
            repository.save(p);
            log.info("Updated price in search read projection for {}: {}", productId, newPrice);
        });
    }

    @Transactional
    public void updateStock(String productId, boolean inStock, int quantity) {
        repository.findById(productId).ifPresent(p -> {
            p.setInStock(inStock);
            p.setStockQuantity(quantity);
            p.setUpdatedAt(Instant.now());
            repository.save(p);
            log.info("Updated stock in search read projection for {}: inStock={}, qty={}", productId, inStock, quantity);
        });
    }

    @Transactional
    public void deleteProduct(String productId) {
        if (repository.existsById(productId)) {
            repository.deleteById(productId);
            log.info("Removed product {} from search read projection", productId);
        }
    }

    @Transactional
    public int rebuildIndex() {
        // Seeds baseline canonical catalog projection demonstrating reindexing
        List<IndexProductRequest> defaults = List.of(
                new IndexProductRequest("PROD-001", "SKU-MECH-KB", "Pulse Mechanical Keyboard",
                        "RGB tactile mechanical keyboard with hot-swappable switches", "CAT-TECH", "Electronics",
                        new BigDecimal("129.99"), "USD", true, 50, "keyboard,rgb,gaming"),
                new IndexProductRequest("PROD-002", "SKU-MOUSE-WL", "Pulse Wireless Gaming Mouse",
                        "Ultra-lightweight 2.4GHz wireless gaming mouse with optical sensor", "CAT-TECH", "Electronics",
                        new BigDecimal("79.99"), "USD", true, 100, "mouse,wireless,gaming"),
                new IndexProductRequest("PROD-003", "SKU-MON-4K", "Pulse 32-inch 4K Monitor",
                        "144Hz IPS gaming and productivity monitor with HDR400", "CAT-TECH", "Electronics",
                        new BigDecimal("499.99"), "USD", true, 20, "monitor,4k,display"),
                new IndexProductRequest("PROD-004", "SKU-DESK-PAD", "Pulse Ergonomic Desk Mat",
                        "Large waterproof desk pad with non-slip rubber base", "CAT-OFFICE", "Office",
                        new BigDecimal("29.99"), "USD", true, 200, "desk,office,accessories"),
                new IndexProductRequest("PROD-005", "SKU-HEADSET-PRO", "Pulse Pro Noise-Cancelling Headset",
                        "Over-ear studio headphones with active noise cancellation and USB-C", "CAT-AUDIO", "Audio",
                        new BigDecimal("199.99"), "USD", false, 0, "audio,headphones,anc")
        );

        defaults.forEach(this::indexProduct);
        log.info("Search projection rebuild completed with {} catalog items", defaults.size());
        return defaults.size();
    }

    private SearchResultDto toDto(ProductSearchIndex p) {
        return new SearchResultDto(
                p.getProductId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getCategoryId(),
                p.getCategoryName(),
                p.getPrice(),
                p.getCurrency(),
                p.isInStock(),
                p.getStockQuantity(),
                p.getTags(),
                p.getUpdatedAt()
        );
    }
}
