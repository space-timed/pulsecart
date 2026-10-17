package com.pulsecart.search.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.search.dto.SearchDtos.IndexProductRequest;
import com.pulsecart.search.service.SearchService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SearchEventListener {

    private static final Logger log = LoggerFactory.getLogger(SearchEventListener.class);

    private final SearchService searchService;
    private final ObjectMapper objectMapper;

    public SearchEventListener(SearchService searchService, ObjectMapper objectMapper) {
        this.searchService = searchService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = PulseCartTopics.CATALOG_EVENTS, groupId = "pulsecart-search-group")
    public void onCatalogEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "";
            JsonNode payload = root.get("payload");

            if (payload == null) return;

            String productId = payload.has("productId") ? payload.get("productId").asText() : payload.has("id") ? payload.get("id").asText() : "";
            if (productId.isBlank()) return;

            if ("ProductCreated".equalsIgnoreCase(eventType) || "ProductUpdated".equalsIgnoreCase(eventType)) {
                String name = payload.has("name") ? payload.get("name").asText() : "Product";
                String sku = payload.has("sku") ? payload.get("sku").asText() : productId;
                String desc = payload.has("description") ? payload.get("description").asText() : "";
                String categoryId = payload.has("categoryId") ? payload.get("categoryId").asText() : null;
                String categoryName = payload.has("categoryName") ? payload.get("categoryName").asText() : "General";
                BigDecimal price = payload.has("price") ? new BigDecimal(payload.get("price").asText()) : BigDecimal.ZERO;

                IndexProductRequest request = new IndexProductRequest(
                        productId, sku, name, desc, categoryId, categoryName, price, "USD", true, 10, ""
                );
                searchService.indexProduct(request);
            } else if ("ProductDeleted".equalsIgnoreCase(eventType)) {
                searchService.deleteProduct(productId);
            }
        } catch (Exception e) {
            log.warn("Error consuming catalog event in search projection: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = PulseCartTopics.PRICING_EVENTS, groupId = "pulsecart-search-group")
    public void onPricingEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            JsonNode payload = root.get("payload");
            if (payload == null) return;

            String productId = payload.has("productId") ? payload.get("productId").asText() : "";
            if (!productId.isBlank() && payload.has("price")) {
                BigDecimal newPrice = new BigDecimal(payload.get("price").asText());
                searchService.updatePrice(productId, newPrice);
            }
        } catch (Exception e) {
            log.warn("Error consuming pricing event in search projection: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = PulseCartTopics.INVENTORY_EVENTS, groupId = "pulsecart-search-group")
    public void onInventoryEvent(ConsumerRecord<String, String> record) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "";
            JsonNode payload = root.get("payload");
            if (payload == null) return;

            String productId = payload.has("productId") ? payload.get("productId").asText() : "";
            if (!productId.isBlank()) {
                if ("StockDepleted".equalsIgnoreCase(eventType)) {
                    searchService.updateStock(productId, false, 0);
                } else if (payload.has("availableQuantity")) {
                    int qty = payload.get("availableQuantity").asInt();
                    searchService.updateStock(productId, qty > 0, qty);
                }
            }
        } catch (Exception e) {
            log.warn("Error consuming inventory event in search projection: {}", e.getMessage());
        }
    }
}
