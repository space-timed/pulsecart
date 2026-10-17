package com.pulsecart.fulfillment.repository;

import com.pulsecart.fulfillment.domain.FulfillmentOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FulfillmentOutboxEventRepository extends JpaRepository<FulfillmentOutboxEvent, Long> {
    List<FulfillmentOutboxEvent> findTop50ByPublishedFalseOrderByCreatedAtAsc();
}
