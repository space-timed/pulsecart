package com.pulsecart.analytics.service;

import com.pulsecart.analytics.domain.HourlySalesAggregate;
import com.pulsecart.analytics.domain.ProductSalesVelocity;
import com.pulsecart.analytics.dto.AnalyticsDtos.*;
import com.pulsecart.analytics.repository.HourlySalesAggregateRepository;
import com.pulsecart.analytics.repository.ProductSalesVelocityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final HourlySalesAggregateRepository hourlyRepository;
    private final ProductSalesVelocityRepository velocityRepository;

    public AnalyticsService(HourlySalesAggregateRepository hourlyRepository,
                            ProductSalesVelocityRepository velocityRepository) {
        this.hourlyRepository = hourlyRepository;
        this.velocityRepository = velocityRepository;
    }

    @Transactional
    public void recordOrderSale(RecordOrderSaleRequest request) {
        String hourBucket = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.HOURS));

        // 1. Update Hourly Sales Aggregate
        HourlySalesAggregate aggregate = hourlyRepository.findById(hourBucket)
                .orElse(new HourlySalesAggregate(hourBucket, 0, BigDecimal.ZERO, 0));

        int totalUnits = 0;
        if (request.items() != null) {
            for (ItemSaleDto item : request.items()) {
                totalUnits += item.quantity();

                // 2. Update Product Sales Velocity
                BigDecimal lineRevenue = item.price().multiply(BigDecimal.valueOf(item.quantity()));
                ProductSalesVelocity velocity = velocityRepository.findById(item.productId())
                        .orElse(new ProductSalesVelocity(item.productId(), item.productName(), 0, BigDecimal.ZERO));

                velocity.addSale(item.quantity(), lineRevenue);
                velocityRepository.save(velocity);
            }
        }

        aggregate.addSale(request.totalAmount(), totalUnits);
        hourlyRepository.save(aggregate);

        log.info("Recorded analytics for order {} [total: {}, units: {}, hour: {}]",
                request.orderId(), request.totalAmount(), totalUnits, hourBucket);
    }

    @Transactional(readOnly = true)
    public AnalyticsOverviewDto getOverview() {
        List<HourlySalesAggregate> allHours = hourlyRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;
        int totalItemsSold = 0;

        for (HourlySalesAggregate h : allHours) {
            totalRevenue = totalRevenue.add(h.getTotalRevenue());
            totalOrders += h.getTotalOrders();
            totalItemsSold += h.getTotalItemsSold();
        }

        int activeProducts = (int) velocityRepository.count();

        return new AnalyticsOverviewDto(
                totalRevenue,
                totalOrders,
                totalItemsSold,
                activeProducts,
                Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public List<ProductVelocityDto> getTopVelocity(int limit) {
        return velocityRepository.findTopByOrderByVelocity(PageRequest.of(0, Math.max(1, limit))).stream()
                .map(v -> new ProductVelocityDto(
                        v.getProductId(),
                        v.getProductName(),
                        v.getUnitsSold(),
                        v.getTotalRevenue(),
                        v.getVelocityScore(),
                        v.getLastOrderAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HourlySalesDto> getHourlyBreakdown() {
        return hourlyRepository.findAllByOrderByHourBucketDesc().stream()
                .map(h -> new HourlySalesDto(
                        h.getHourBucket(),
                        h.getTotalOrders(),
                        h.getTotalRevenue(),
                        h.getTotalItemsSold()
                ))
                .toList();
    }
}
