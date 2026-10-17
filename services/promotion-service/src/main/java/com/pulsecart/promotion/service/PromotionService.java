package com.pulsecart.promotion.service;

import com.pulsecart.promotion.domain.Coupon;
import com.pulsecart.promotion.domain.CouponRedemption;
import com.pulsecart.promotion.domain.DiscountType;
import com.pulsecart.promotion.dto.PromotionDtos.*;
import com.pulsecart.promotion.repository.CouponRedemptionRepository;
import com.pulsecart.promotion.repository.CouponRepository;
import com.pulsecart.promotion.specification.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class PromotionService {

    private final CouponRepository couponRepository;
    private final CouponRedemptionRepository redemptionRepository;
    private final PromotionSpecification ruleEngine;

    public PromotionService(CouponRepository couponRepository, CouponRedemptionRepository redemptionRepository) {
        this.couponRepository = couponRepository;
        this.redemptionRepository = redemptionRepository;
        this.ruleEngine = new ActiveStatusSpecification()
                .and(new ValidityPeriodSpecification())
                .and(new MaxRedemptionsSpecification())
                .and(new MinimumOrderAmountSpecification())
                .and(new CustomerTierSpecification());
    }

    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest req) {
        if (couponRepository.existsByCode(req.code().toUpperCase())) {
            throw new IllegalArgumentException("Coupon code already exists: " + req.code());
        }

        Coupon coupon = new Coupon(
                req.code().toUpperCase(),
                req.description(),
                req.discountType(),
                req.discountValue(),
                req.minOrderAmount(),
                req.maxDiscountAmount(),
                req.maxRedemptions(),
                req.startsAt(),
                req.expiresAt(),
                req.customerTierRequired()
        );

        Coupon saved = couponRepository.save(coupon);
        return toCouponResponse(saved);
    }

    public Optional<CouponResponse> getCouponByCode(String code) {
        return couponRepository.findByCode(code.toUpperCase()).map(this::toCouponResponse);
    }

    public CouponValidationResponse validateCoupon(ValidateCouponRequest req) {
        Optional<Coupon> opt = couponRepository.findByCode(req.code().toUpperCase());
        if (opt.isEmpty()) {
            return new CouponValidationResponse(
                    false, req.code(), null, BigDecimal.ZERO, BigDecimal.ZERO, req.orderAmount(), "Coupon code not found"
            );
        }

        Coupon coupon = opt.get();
        PromotionContext context = new PromotionContext(
                coupon,
                req.orderAmount(),
                req.customerId(),
                req.customerTier()
        );

        ValidationResult result = ruleEngine.validate(context);
        if (!result.valid()) {
            return new CouponValidationResponse(
                    false, coupon.getCode(), coupon.getDiscountType(), coupon.getDiscountValue(),
                    BigDecimal.ZERO, req.orderAmount(), result.message()
            );
        }

        BigDecimal discount = calculateDiscount(coupon, req.orderAmount());
        BigDecimal finalAmount = req.orderAmount().subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_EVEN);

        return new CouponValidationResponse(
                true, coupon.getCode(), coupon.getDiscountType(), coupon.getDiscountValue(),
                discount, finalAmount, "Coupon is valid and applicable"
        );
    }

    @Transactional
    public CouponRedemptionResponse applyCoupon(ApplyCouponRequest req) {
        Optional<Coupon> opt = couponRepository.findByCode(req.code().toUpperCase());
        if (opt.isEmpty()) {
            return new CouponRedemptionResponse(false, req.code(), req.orderId(), BigDecimal.ZERO, req.orderAmount(), "Coupon not found");
        }

        Coupon coupon = opt.get();

        if (redemptionRepository.existsByCouponIdAndOrderId(coupon.getId(), req.orderId())) {
            return new CouponRedemptionResponse(false, coupon.getCode(), req.orderId(), BigDecimal.ZERO, req.orderAmount(), "Coupon already redeemed for this order");
        }

        PromotionContext context = new PromotionContext(coupon, req.orderAmount(), req.customerId(), req.customerTier());
        ValidationResult result = ruleEngine.validate(context);
        if (!result.valid()) {
            return new CouponRedemptionResponse(false, coupon.getCode(), req.orderId(), BigDecimal.ZERO, req.orderAmount(), result.message());
        }

        // Atomic conditional update guarantees no over-redemption under flash-sale competition
        int updated = couponRepository.incrementRedemptionAtomic(coupon.getId());
        if (updated == 0) {
            return new CouponRedemptionResponse(false, coupon.getCode(), req.orderId(), BigDecimal.ZERO, req.orderAmount(), "Coupon redemption limit reached during concurrent processing");
        }

        BigDecimal discount = calculateDiscount(coupon, req.orderAmount());
        BigDecimal finalAmount = req.orderAmount().subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_EVEN);

        CouponRedemption redemption = new CouponRedemption(coupon, req.orderId(), req.customerId(), discount);
        redemptionRepository.save(redemption);

        return new CouponRedemptionResponse(true, coupon.getCode(), req.orderId(), discount, finalAmount, "Coupon successfully applied");
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal percentage = coupon.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);
            discount = orderAmount.multiply(percentage).setScale(2, RoundingMode.HALF_EVEN);
            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            discount = coupon.getDiscountValue().min(orderAmount).setScale(2, RoundingMode.HALF_EVEN);
        }

        return discount;
    }

    private CouponResponse toCouponResponse(Coupon c) {
        return new CouponResponse(
                c.getId(),
                c.getCode(),
                c.getDescription(),
                c.getDiscountType(),
                c.getDiscountValue(),
                c.getMinOrderAmount(),
                c.getMaxDiscountAmount(),
                c.getMaxRedemptions(),
                c.getCurrentRedemptions(),
                c.getStartsAt(),
                c.getExpiresAt(),
                c.isActive(),
                c.getCustomerTierRequired()
        );
    }
}
