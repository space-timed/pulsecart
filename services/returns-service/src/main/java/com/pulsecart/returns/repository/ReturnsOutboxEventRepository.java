package com.pulsecart.returns.repository;

import com.pulsecart.returns.domain.ReturnsOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnsOutboxEventRepository extends JpaRepository<ReturnsOutboxEvent, String> {
    List<ReturnsOutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
