package com.pulsecart.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dlq_messages", schema = "pulsecart_notification")
public class DlqMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(name = "partition_id", nullable = false)
    private int partitionId = 0;

    @Column(name = "offset_val", nullable = false)
    private long offsetVal = 0;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "exception_message", columnDefinition = "TEXT")
    private String exceptionMessage;

    @Column(nullable = false)
    private boolean replayed = false;

    @Column(name = "failed_at", nullable = false, updatable = false)
    private Instant failedAt = Instant.now();

    public DlqMessage() {}

    public DlqMessage(String topic, int partitionId, long offsetVal, String payload, String exceptionMessage) {
        this.topic = topic;
        this.partitionId = partitionId;
        this.offsetVal = offsetVal;
        this.payload = payload;
        this.exceptionMessage = exceptionMessage;
        this.replayed = false;
        this.failedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public int getPartitionId() { return partitionId; }
    public void setPartitionId(int partitionId) { this.partitionId = partitionId; }
    public long getOffsetVal() { return offsetVal; }
    public void setOffsetVal(long offsetVal) { this.offsetVal = offsetVal; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getExceptionMessage() { return exceptionMessage; }
    public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }
    public boolean isReplayed() { return replayed; }
    public void setReplayed(boolean replayed) { this.replayed = replayed; }
    public Instant getFailedAt() { return failedAt; }
}
