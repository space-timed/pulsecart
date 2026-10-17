package com.pulsecart.contracts.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventEnvelopeTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Should correctly serialize and deserialize EventEnvelope with OrderCreatedPayload")
    void shouldSerializeAndDeserializeOrderEvent() throws Exception {
        OrderPayloads.OrderItemPayload item = new OrderPayloads.OrderItemPayload(
                "prod-101", "SKU-AURORA-1", "Aurora Noise-Cancelling Headphones", 2, new BigDecimal("199.99")
        );

        OrderPayloads.OrderCreatedPayload payload = new OrderPayloads.OrderCreatedPayload(
                "ord-9901", "cust-4412", new BigDecimal("399.98"), "USD", List.of(item)
        );

        EventEnvelope<OrderPayloads.OrderCreatedPayload> envelope = EventEnvelope.create(
                "order.created",
                1,
                "ord-9901",
                "Order",
                "corr-5501",
                "cmd-1001",
                "order-service",
                payload
        );

        String json = objectMapper.writeValueAsString(envelope);
        assertThat(json).contains("ord-9901");
        assertThat(json).contains("Aurora Noise-Cancelling Headphones");
        assertThat(json).contains("order-service");

        EventEnvelope<OrderPayloads.OrderCreatedPayload> deserialized = objectMapper.readValue(
                json, new TypeReference<EventEnvelope<OrderPayloads.OrderCreatedPayload>>() {}
        );

        assertThat(deserialized.eventId()).isEqualTo(envelope.eventId());
        assertThat(deserialized.eventType()).isEqualTo("order.created");
        assertThat(deserialized.aggregateId()).isEqualTo("ord-9901");
        assertThat(deserialized.payload().items()).hasSize(1);
        assertThat(deserialized.payload().items().getFirst().sku()).isEqualTo("SKU-AURORA-1");
    }
}
