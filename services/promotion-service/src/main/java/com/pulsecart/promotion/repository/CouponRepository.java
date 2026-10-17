package com.pulsecart.promotion.repository;

import com.pulsecart.promotion.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    @Modifying
    @Query("UPDATE Coupon c SET c.currentRedemptions = c.currentRedemptions + 1, c.updatedAt = CURRENT_INSTANT " +
           "WHERE c.id = :id AND c.currentRedemptions < c.maxRedemptions AND c.active = true")
    int incrementRedemptionAtomic(@Param("id") Long id);
}
