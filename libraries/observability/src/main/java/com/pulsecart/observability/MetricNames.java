package com.pulsecart.observability;

public final class MetricNames {
    private MetricNames() {}

    // Commerce Operations
    public static final String ORDERS_CREATED = "pulsecart.orders.created.total";
    public static final String ORDERS_CONFIRMED = "pulsecart.orders.confirmed.total";
    public static final String ORDERS_CANCELLED = "pulsecart.orders.cancelled.total";
    public static final String SAGA_DURATION = "pulsecart.saga.duration.seconds";

    // Inventory Metrics
    public static final String INVENTORY_RESERVATIONS = "pulsecart.inventory.reservations.total";
    public static final String INVENTORY_CONFLICTS = "pulsecart.inventory.conflicts.total";
    public static final String INVENTORY_RELEASES = "pulsecart.inventory.releases.total";

    // Payment Metrics
    public static final String PAYMENT_ATTEMPTS = "pulsecart.payment.attempts.total";
    public static final String PAYMENT_FAILURES = "pulsecart.payment.failures.total";
    public static final String PAYMENT_LATENCY = "pulsecart.payment.duration.seconds";

    // Outbox & Kafka Metrics
    public static final String OUTBOX_PUBLISHED = "pulsecart.outbox.published.total";
    public static final String OUTBOX_ERRORS = "pulsecart.outbox.errors.total";
    public static final String KAFKA_CONSUMER_RETRIES = "pulsecart.kafka.retries.total";
    public static final String KAFKA_DLQ_MESSAGES = "pulsecart.kafka.dlq.total";
}
