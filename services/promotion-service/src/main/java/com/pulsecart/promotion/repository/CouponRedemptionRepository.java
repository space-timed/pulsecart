package com.pulsecart.promotion.repository;

import com.pulsecart.promotion.domain.CouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {

    List<CouponRedemption> findByCustomerId(String customerId);

    boolean existsByCouponIdAndOrderId(Long couponId, String orderId);
}
