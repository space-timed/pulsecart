package com.pulsecart.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationContextTest {

    @AfterEach
    void tearDown() {
        CorrelationContext.clear();
    }

    @Test
    @DisplayName("Should generate new correlation ID when none is set")
    void shouldGenerateNewCorrelationIdWhenNoneSet() {
        String id = CorrelationContext.getOrCreate();
        assertThat(id).isNotBlank();
        assertThat(CorrelationContext.get()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should retrieve existing correlation ID when explicitly set")
    void shouldRetrieveExplicitlySetCorrelationId() {
        CorrelationContext.set("corr-test-123");
        assertThat(CorrelationContext.get()).isEqualTo("corr-test-123");
        assertThat(CorrelationContext.getOrCreate()).isEqualTo("corr-test-123");
    }

    @Test
    @DisplayName("Should clear context cleanly")
    void shouldClearContext() {
        CorrelationContext.set("corr-test-123");
        CorrelationContext.clear();
        assertThat(CorrelationContext.get()).isNull();
    }
}
