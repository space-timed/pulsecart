package com.pulsecart.inventory.repository;

import com.pulsecart.inventory.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, String> {
    Optional<StockReservation> findByOrderId(String orderId);
    List<StockReservation> findByStatus(String status);
}
