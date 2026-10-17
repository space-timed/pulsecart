package com.pulsecart.catalog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.catalog.domain.*;
import com.pulsecart.catalog.dto.CatalogDtos.*;
import com.pulsecart.catalog.repository.CategoryRepository;
import com.pulsecart.catalog.repository.OutboxEventRepository;
import com.pulsecart.catalog.repository.ProductRepository;
import com.pulsecart.contracts.event.CatalogPayloads.ProductCreatedPayload;
import com.pulsecart.contracts.event.CatalogPayloads.ProductUpdatedPayload;
import com.pulsecart.contracts.event.EventEnvelope;
import com.pulsecart.observability.CorrelationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CatalogService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          OutboxEventRepository outboxEventRepository,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Category with name '" + request.name() + "' already exists");
        }
        Category category = new Category(request.name(), request.slug(), request.description(), request.parentId());
        Category saved = categoryRepository.save(category);
        return toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::toCategoryResponse).toList();
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new IllegalArgumentException("Product with SKU '" + request.sku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.categoryId()));

        Product product = new Product(
                request.sku(),
                request.name(),
                request.slug(),
                request.description(),
                category,
                request.basePrice(),
                request.imageUrl(),
                request.tags()
        );

        if (request.specifications() != null) {
            for (SpecificationDto spec : request.specifications()) {
                product.addSpecification(spec.key(), spec.value());
            }
        }

        Product saved = productRepository.save(product);

        // Transactional Outbox: atomically commit outbox event with product
        saveOutboxEvent(
                "Product",
                saved.getId(),
                "catalog.product-created",
                new ProductCreatedPayload(
                        saved.getId(),
                        saved.getSku(),
                        saved.getName(),
                        saved.getDescription(),
                        saved.getCategory().getId(),
                        saved.getBasePrice(),
                        saved.getTags() != null ? Arrays.asList(saved.getTags().split(",")) : List.of()
                )
        );

        return toProductResponse(saved);
    }

    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        ProductStatus status = ProductStatus.valueOf(request.status().toUpperCase());
        product.update(
                request.name(),
                request.description(),
                request.basePrice(),
                status,
                request.imageUrl(),
                request.tags()
        );

        Product saved = productRepository.save(product);

        // Transactional Outbox: atomic event commit
        saveOutboxEvent(
                "Product",
                saved.getId(),
                "catalog.product-updated",
                new ProductUpdatedPayload(
                        saved.getId(),
                        saved.getSku(),
                        saved.getName(),
                        saved.getDescription(),
                        saved.getCategory().getId(),
                        saved.getBasePrice(),
                        saved.getStatus().name()
                )
        );

        return toProductResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        return toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
        return toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable).map(this::toProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(this::toProductResponse);
    }

    private <T> void saveOutboxEvent(String aggregateType, String aggregateId, String eventType, T payload) {
        try {
            EventEnvelope<T> envelope = EventEnvelope.create(
                    eventType,
                    1,
                    aggregateId,
                    aggregateType,
                    CorrelationContext.getOrCreate(),
                    null,
                    "catalog-service",
                    payload
            );
            String jsonPayload = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = new OutboxEvent(aggregateType, aggregateId, eventType, jsonPayload);
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event for " + eventType, e);
        }
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParentId(),
                category.getCreatedAt()
        );
    }

    private ProductResponse toProductResponse(Product product) {
        List<SpecificationDto> specs = product.getSpecifications().stream()
                .map(s -> new SpecificationDto(s.getSpecKey(), s.getSpecValue()))
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getBasePrice(),
                product.getStatus().name(),
                product.getImageUrl(),
                product.getTags(),
                specs,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
