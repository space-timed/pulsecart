package com.pulsecart.promotion.specification;

import com.pulsecart.promotion.domain.Coupon;
import java.math.BigDecimal;

public record PromotionContext(
        Coupon coupon,
        BigDecimal orderAmount,
        String customerId,
        String customerTier
) {}
