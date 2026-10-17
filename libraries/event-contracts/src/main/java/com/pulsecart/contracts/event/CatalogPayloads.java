package com.pulsecart.contracts.event;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogPayloads {

    private CatalogPayloads() {}

    public record ProductCreatedPayload(
            String productId,
            String sku,
            String name,
            String description,
            String categoryId,
            BigDecimal basePrice,
            List<String> tags
    ) {}

    public record ProductUpdatedPayload(
            String productId,
            String sku,
            String name,
            String description,
            String categoryId,
            BigDecimal basePrice,
            String status
    ) {}
}
