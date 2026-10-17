package com.pulsecart.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    public record AnalyticsOverviewDto(
            BigDecimal totalRevenue,
            int totalOrders,
            int totalItemsSold,
            int activeVelocityProducts,
            Instant timestamp
    ) {}

    public record ProductVelocityDto(
            String productId,
            String productName,
            int unitsSold,
            BigDecimal totalRevenue,
            BigDecimal velocityScore,
            Instant lastOrderAt
    ) {}

    public record HourlySalesDto(
            String hourBucket,
            int totalOrders,
            BigDecimal totalRevenue,
            int totalItemsSold
    ) {}

    public record RecordOrderSaleRequest(
            String orderId,
            String customerId,
            BigDecimal totalAmount,
            List<ItemSaleDto> items
    ) {}

    public record ItemSaleDto(
            String productId,
            String productName,
            int quantity,
            BigDecimal price
    ) {}
}
