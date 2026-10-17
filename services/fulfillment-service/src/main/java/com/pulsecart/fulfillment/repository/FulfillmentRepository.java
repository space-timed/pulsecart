package com.pulsecart.fulfillment.repository;

import com.pulsecart.fulfillment.domain.Fulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, String> {
    Optional<Fulfillment> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}
