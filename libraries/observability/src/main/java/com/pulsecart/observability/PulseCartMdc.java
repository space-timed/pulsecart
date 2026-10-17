package com.pulsecart.observability;

public final class PulseCartMdc {
    private PulseCartMdc() {}

    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String USER_ID = "userId";
    public static final String SERVICE_NAME = "serviceName";
    public static final String EVENT_TYPE = "eventType";
    public static final String SAGA_ID = "sagaId";
}
