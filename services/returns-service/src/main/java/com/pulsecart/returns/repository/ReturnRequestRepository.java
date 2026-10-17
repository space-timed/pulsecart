package com.pulsecart.returns.repository;

import com.pulsecart.returns.domain.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, String> {
    List<ReturnRequest> findByOrderId(String orderId);
    List<ReturnRequest> findByCustomerId(String customerId);
}
