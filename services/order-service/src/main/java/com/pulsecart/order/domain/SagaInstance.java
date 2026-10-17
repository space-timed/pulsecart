package com.pulsecart.order.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saga_instances", schema = "pulsecart_order")
public class SagaInstance {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "current_step", nullable = false, length = 50)
    private String currentStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SagaStatus status;

    @Column(name = "compensation_reason", length = 255)
    private String compensationReason;

    @OneToMany(mappedBy = "sagaInstance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SagaStepLog> stepLogs = new ArrayList<>();

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public SagaInstance() {}

    public SagaInstance(String id, String orderId, String initialStep) {
        this.id = id;
        this.orderId = orderId;
        this.currentStep = initialStep;
        this.status = SagaStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    public void addStepLog(SagaStepLog log) {
        stepLogs.add(log);
        log.setSagaInstance(this);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
    public SagaStatus getStatus() { return status; }
    public void setStatus(SagaStatus status) { this.status = status; }
    public String getCompensationReason() { return compensationReason; }
    public void setCompensationReason(String compensationReason) { this.compensationReason = compensationReason; }
    public List<SagaStepLog> getStepLogs() { return stepLogs; }
    public void setStepLogs(List<SagaStepLog> stepLogs) { this.stepLogs = stepLogs; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
