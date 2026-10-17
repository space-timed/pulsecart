package com.pulsecart.shipping.repository;

import com.pulsecart.shipping.domain.ShippingOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingOutboxEventRepository extends JpaRepository<ShippingOutboxEvent, Long> {
    List<ShippingOutboxEvent> findTop50ByPublishedFalseOrderByCreatedAtAsc();
}
