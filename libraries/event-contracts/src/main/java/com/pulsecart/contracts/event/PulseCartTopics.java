package com.pulsecart.contracts.event;

/**
 * Authoritative Kafka topic declarations across the PulseCart microservices platform.
 */
public final class PulseCartTopics {

    private PulseCartTopics() {}

    public static final String CUSTOMER_EVENTS = "pulsecart.customer.events";
    public static final String CATALOG_EVENTS = "pulsecart.catalog.events";
    public static final String PRICING_EVENTS = "pulsecart.pricing.events";
    public static final String PROMOTION_EVENTS = "pulsecart.promotion.events";
    public static final String INVENTORY_EVENTS = "pulsecart.inventory.events";
    public static final String ORDER_EVENTS = "pulsecart.order.events";
    public static final String PAYMENT_EVENTS = "pulsecart.payment.events";
    public static final String FULFILLMENT_EVENTS = "pulsecart.fulfillment.events";
    public static final String SHIPPING_EVENTS = "pulsecart.shipping.events";
    public static final String NOTIFICATION_EVENTS = "pulsecart.notification.events";
    public static final String RETURNS_EVENTS = "pulsecart.returns.events";

    // Dead Letter Queues
    public static final String NOTIFICATION_DLQ = "pulsecart.notification.events.dlq";
    public static final String ORDER_DLQ = "pulsecart.order.events.dlq";
    public static final String PAYMENT_DLQ = "pulsecart.payment.events.dlq";
}
