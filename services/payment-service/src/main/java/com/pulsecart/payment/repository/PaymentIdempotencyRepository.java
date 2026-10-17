package com.pulsecart.payment.repository;

import com.pulsecart.payment.domain.PaymentIdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentIdempotencyRepository extends JpaRepository<PaymentIdempotencyRecord, String> {
    Optional<PaymentIdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}
