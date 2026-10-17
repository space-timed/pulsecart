package com.pulsecart.fulfillment.repository;

import com.pulsecart.fulfillment.domain.FulfillmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FulfillmentItemRepository extends JpaRepository<FulfillmentItem, Long> {
    List<FulfillmentItem> findByFulfillmentId(String fulfillmentId);
}
