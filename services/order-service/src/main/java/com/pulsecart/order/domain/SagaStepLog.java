package com.pulsecart.order.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "saga_step_logs", schema = "pulsecart_order")
public class SagaStepLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "saga_id", nullable = false)
    private SagaInstance sagaInstance;

    @Column(name = "step_name", nullable = false, length = 50)
    private String stepName;

    @Column(nullable = false, length = 30)
    private String status; // PENDING, SUCCESS, FAILED, COMPENSATED

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private Instant executedAt = Instant.now();

    public SagaStepLog() {}

    public SagaStepLog(SagaInstance sagaInstance, String stepName, String status, String payload, String errorMessage) {
        this.sagaInstance = sagaInstance;
        this.stepName = stepName;
        this.status = status;
        this.payload = payload;
        this.errorMessage = errorMessage;
        this.executedAt = Instant.now();
    }

    public Long getId() { return id; }
    public SagaInstance getSagaInstance() { return sagaInstance; }
    public void setSagaInstance(SagaInstance sagaInstance) { this.sagaInstance = sagaInstance; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getExecutedAt() { return executedAt; }
}
