package com.pulsecart.pricing.controller;

import com.pulsecart.pricing.dto.PricingDtos.*;
import com.pulsecart.pricing.service.PricingService;
import com.pulsecart.pricing.strategy.PricingCalculationResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prices")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping
    public ResponseEntity<PriceResponse> setPrice(@Valid @RequestBody SetPriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingService.setProductPrice(request));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<PriceResponse> getPrice(@PathVariable String productId) {
        return ResponseEntity.ok(pricingService.getProductPrice(productId));
    }

    @PostMapping("/calculate")
    public ResponseEntity<PricingCalculationResult> calculatePrice(@Valid @RequestBody CalculatePriceRequest request) {
        return ResponseEntity.ok(pricingService.calculatePrice(request));
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<List<PriceHistoryResponse>> getPriceHistory(@PathVariable String productId) {
        return ResponseEntity.ok(pricingService.getPriceHistory(productId));
    }
}
