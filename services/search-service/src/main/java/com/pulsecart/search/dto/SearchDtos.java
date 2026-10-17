package com.pulsecart.search.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class SearchDtos {

    private SearchDtos() {}

    public record SearchResultDto(
            String productId,
            String sku,
            String name,
            String description,
            String categoryId,
            String categoryName,
            BigDecimal price,
            String currency,
            boolean inStock,
            int stockQuantity,
            String tags,
            Instant updatedAt
    ) {}

    public record SearchResponse(
            String query,
            int total,
            List<SearchResultDto> results
    ) {}

    public record IndexProductRequest(
            String productId,
            String sku,
            String name,
            String description,
            String categoryId,
            String categoryName,
            BigDecimal price,
            String currency,
            Boolean inStock,
            Integer stockQuantity,
            String tags
    ) {}
}
