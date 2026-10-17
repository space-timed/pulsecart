package com.pulsecart.gateway.filter;

import com.pulsecart.observability.TelemetryHeaders;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * WebFilter ensuring every request entering the API Gateway contains a distributed correlation ID,
 * forwarded to downstream services and returned in the HTTP response headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdGlobalFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(TelemetryHeaders.CORRELATION_ID);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String finalCorrelationId = correlationId;

        // Ensure the response header is written before headers are committed
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().set(TelemetryHeaders.CORRELATION_ID, finalCorrelationId);
            return Mono.empty();
        });

        // Mutate request with correlation header for downstream routing & internal handlers
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TelemetryHeaders.CORRELATION_ID, finalCorrelationId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
