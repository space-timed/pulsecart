package com.pulsecart.observability;

import java.util.UUID;

/**
 * Thread-local context holder for correlation IDs across synchronous request execution.
 */
public final class CorrelationContext {

    private static final ThreadLocal<String> CURRENT_CORRELATION_ID = new ThreadLocal<>();

    private CorrelationContext() {}

    public static void set(String correlationId) {
        CURRENT_CORRELATION_ID.set(correlationId);
    }

    public static String get() {
        return CURRENT_CORRELATION_ID.get();
    }

    public static String getOrCreate() {
        String id = CURRENT_CORRELATION_ID.get();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            CURRENT_CORRELATION_ID.set(id);
        }
        return id;
    }

    public static void clear() {
        CURRENT_CORRELATION_ID.remove();
    }
}
