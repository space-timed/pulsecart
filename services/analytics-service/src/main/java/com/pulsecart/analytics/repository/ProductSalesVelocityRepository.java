package com.pulsecart.analytics.repository;

import com.pulsecart.analytics.domain.ProductSalesVelocity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSalesVelocityRepository extends JpaRepository<ProductSalesVelocity, String> {

    @Query("SELECT p FROM ProductSalesVelocity p ORDER BY p.unitsSold DESC, p.velocityScore DESC")
    List<ProductSalesVelocity> findTopByOrderByVelocity(Pageable pageable);
}
