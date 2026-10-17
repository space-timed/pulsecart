package com.pulsecart.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class CatalogDtos {

    private CatalogDtos() {}

    public record CreateCategoryRequest(
            @NotBlank(message = "Category name is mandatory")
            String name,
            String slug,
            String description,
            String parentId
    ) {}

    public record CategoryResponse(
            String id,
            String name,
            String slug,
            String description,
            String parentId,
            Instant createdAt
    ) {}

    public record SpecificationDto(
            String key,
            String value
    ) {}

    public record CreateProductRequest(
            @NotBlank(message = "SKU is mandatory")
            String sku,
            @NotBlank(message = "Product name is mandatory")
            String name,
            String slug,
            String description,
            @NotBlank(message = "Category ID is mandatory")
            String categoryId,
            @NotNull(message = "Base price is mandatory")
            @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
            BigDecimal basePrice,
            String imageUrl,
            String tags,
            List<SpecificationDto> specifications
    ) {}

    public record UpdateProductRequest(
            @NotBlank(message = "Product name is mandatory")
            String name,
            String description,
            @NotNull(message = "Base price is mandatory")
            @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
            BigDecimal basePrice,
            String status,
            String imageUrl,
            String tags
    ) {}

    public record ProductResponse(
            String id,
            String sku,
            String name,
            String slug,
            String description,
            String categoryId,
            String categoryName,
            BigDecimal basePrice,
            String status,
            String imageUrl,
            String tags,
            List<SpecificationDto> specifications,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
