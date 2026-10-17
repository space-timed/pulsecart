package com.pulsecart.order.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class HttpInventoryClient implements InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(HttpInventoryClient.class);
    private final RestClient restClient;

    public HttpInventoryClient(@Value("${pulsecart.services.inventory-url:http://localhost:8084}") String inventoryUrl) {
        this.restClient = RestClient.builder().baseUrl(inventoryUrl).build();
    }

    @Override
    public ReservationResult reserveStock(String orderId, List<ReservationItemDto> items) {
        try {
            var body = Map.of(
                    "orderId", orderId,
                    "items", items
            );

            ResponseEntity<Map> response = restClient.post()
                    .uri("/api/v1/inventory/reserve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String reservationId = (String) response.getBody().get("reservationId");
                return new ReservationResult(true, reservationId, null);
            }
            return new ReservationResult(false, null, "Inventory reservation failed with status " + response.getStatusCode());
        } catch (Exception e) {
            log.warn("Inventory reservation call failed: {}", e.getMessage());
            return new ReservationResult(false, null, e.getMessage());
        }
    }

    @Override
    public boolean commitStock(String reservationId) {
        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri("/api/v1/inventory/commit/{id}", reservationId)
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Inventory commit call failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean releaseStock(String reservationId) {
        try {
            ResponseEntity<Void> response = restClient.post()
                    .uri("/api/v1/inventory/release/{id}", reservationId)
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Inventory release call failed: {}", e.getMessage());
            return false;
        }
    }
}
