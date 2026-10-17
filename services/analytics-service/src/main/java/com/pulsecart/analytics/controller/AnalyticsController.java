package com.pulsecart.analytics.controller;

import com.pulsecart.analytics.dto.AnalyticsDtos.*;
import com.pulsecart.analytics.service.AnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewDto> getOverview() {
        return ResponseEntity.ok(analyticsService.getOverview());
    }

    @GetMapping("/velocity")
    public ResponseEntity<List<ProductVelocityDto>> getTopVelocity(
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(analyticsService.getTopVelocity(limit));
    }

    @GetMapping("/hourly")
    public ResponseEntity<List<HourlySalesDto>> getHourlyBreakdown() {
        return ResponseEntity.ok(analyticsService.getHourlyBreakdown());
    }

    @PostMapping("/record")
    public ResponseEntity<Void> recordSale(@RequestBody RecordOrderSaleRequest request) {
        analyticsService.recordOrderSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
