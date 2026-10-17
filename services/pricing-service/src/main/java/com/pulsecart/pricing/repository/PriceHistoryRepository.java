package com.pulsecart.pricing.repository;

import com.pulsecart.pricing.domain.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, String> {
    List<PriceHistory> findByProductIdOrderByCreatedAtDesc(String productId);
}
