package com.pulsecart.promotion.controller;

import com.pulsecart.promotion.dto.PromotionDtos.*;
import com.pulsecart.promotion.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promotions/coupons")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = promotionService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<CouponResponse> getCoupon(@PathVariable String code) {
        return promotionService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(@Valid @RequestBody ValidateCouponRequest request) {
        CouponValidationResponse response = promotionService.validateCoupon(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply")
    public ResponseEntity<CouponRedemptionResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        CouponRedemptionResponse response = promotionService.applyCoupon(request);
        if (!response.success()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
        return ResponseEntity.ok(response);
    }
}
