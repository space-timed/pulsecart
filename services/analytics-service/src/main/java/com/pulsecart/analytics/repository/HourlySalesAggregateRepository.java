package com.pulsecart.analytics.repository;

import com.pulsecart.analytics.domain.HourlySalesAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HourlySalesAggregateRepository extends JpaRepository<HourlySalesAggregate, String> {
    List<HourlySalesAggregate> findAllByOrderByHourBucketDesc();
}
