package com.pulsecart.gateway.filter;

import com.pulsecart.observability.TelemetryHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class LoggingGlobalFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getRawPath();
        String method = exchange.getRequest().getMethod().name();
        String correlationId = exchange.getRequest().getHeaders().getFirst(TelemetryHeaders.CORRELATION_ID);

        log.info("[PulseCart-Gateway] INCOMING: {} {} [correlationId={}]", method, path, correlationId);

        return chain.filter(exchange).doFinally(signalType -> {
            long duration = System.currentTimeMillis() - startTime;
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            log.info("[PulseCart-Gateway] OUTGOING: {} {} -> {} (took {}ms) [correlationId={}]",
                    method, path, status != null ? status.value() : "UNKNOWN", duration, correlationId);
        });
    }
}
