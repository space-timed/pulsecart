package com.pulsecart.contracts.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

/**
 * Standard domain event envelope ensuring uniform structure across all asynchronous Kafka communication.
 *
 * @param eventId       Globally unique event identifier (UUID)
 * @param eventType     Fully-qualified event name (e.g., 'order.confirmed')
 * @param eventVersion  Schema version for event evolution and compatibility
 * @param aggregateId   Identifier of the underlying domain aggregate (e.g., order ID)
 * @param aggregateType Type of aggregate (e.g., 'Order', 'Product', 'Customer')
 * @param correlationId Distributed correlation identifier tracing the end-to-end user operation
 * @param causationId   ID of the command or previous event that caused this event
 * @param timestamp     UTC timestamp of event occurrence
 * @param producer      Originating microservice name
 * @param payload       Typed business payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventEnvelope<T>(
        String eventId,
        String eventType,
        int eventVersion,
        String aggregateId,
        String aggregateType,
        String correlationId,
        String causationId,
        Instant timestamp,
        String producer,
        T payload
) {
    public static <T> EventEnvelope<T> create(
            String eventType,
            int eventVersion,
            String aggregateId,
            String aggregateType,
            String correlationId,
            String causationId,
            String producer,
            T payload
    ) {
        return new EventEnvelope<>(
                UUID.randomUUID().toString(),
                eventType,
                eventVersion,
                aggregateId,
                aggregateType,
                correlationId != null ? correlationId : UUID.randomUUID().toString(),
                causationId,
                Instant.now(),
                producer,
                payload
        );
    }
}
