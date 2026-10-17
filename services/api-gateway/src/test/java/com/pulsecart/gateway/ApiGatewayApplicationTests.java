package com.pulsecart.gateway;

import com.pulsecart.observability.TelemetryHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Actuator health endpoint should return UP status")
    void healthEndpointShouldReturnUp() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @DisplayName("Gateway should attach correlation ID in response header")
    void shouldAttachCorrelationIdHeader() {
        webTestClient.get()
                .uri("/actuator/health")
                .header(TelemetryHeaders.CORRELATION_ID, "corr-test-gateway-99")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value(TelemetryHeaders.CORRELATION_ID, value -> {
                    assertThat(value).isEqualTo("corr-test-gateway-99");
                });
    }

    @Test
    @DisplayName("Gateway should auto-generate correlation ID when none is provided")
    void shouldGenerateCorrelationIdWhenMissing() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value(TelemetryHeaders.CORRELATION_ID, value -> {
                    assertThat(value).isNotBlank();
                });
    }

    @Test
    @DisplayName("Fallback controller returns SERVICE_UNAVAILABLE status")
    void fallbackEndpointReturnsServiceUnavailable() {
        webTestClient.get()
                .uri("/fallback/service-unavailable")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("Service Unavailable");
    }
}
