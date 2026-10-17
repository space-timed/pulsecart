package com.pulsecart.observability;

public final class TelemetryHeaders {
    private TelemetryHeaders() {}

    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String CAUSATION_ID = "X-Causation-Id";
    public static final String USER_ID = "X-User-Id";
    public static final String USER_ROLES = "X-User-Roles";
    public static final String REQUEST_ID = "X-Request-Id";
}
