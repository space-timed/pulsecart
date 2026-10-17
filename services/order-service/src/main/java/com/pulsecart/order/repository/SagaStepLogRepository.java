package com.pulsecart.order.repository;

import com.pulsecart.order.domain.SagaStepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SagaStepLogRepository extends JpaRepository<SagaStepLog, Long> {
    List<SagaStepLog> findBySagaInstanceIdOrderByExecutedAtAsc(String sagaId);
}
