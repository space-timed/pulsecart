package com.pulsecart.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.contracts.event.PulseCartTopics;
import com.pulsecart.search.listener.SearchEventListener;
import com.pulsecart.search.service.SearchService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchEventListener eventListener;

    @BeforeEach
    void setup() {
        searchService.rebuildIndex();
    }

    @Test
    @DisplayName("Should query search read projection by text keyword, category, price range, and sort order")
    void testRebuildIndexAndFacetedSearch() throws Exception {
        // Query keyword "keyboard"
        mockMvc.perform(get("/api/v1/search?q=keyboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.results[0].name").value("Pulse Mechanical Keyboard"))
                .andExpect(jsonPath("$.results[0].price").value(129.99));

        // Faceted filtering: Category "Electronics", price between $100 and $600
        mockMvc.perform(get("/api/v1/search?category=Electronics&minPrice=100&maxPrice=600"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));

        // Sort by price ascending
        mockMvc.perform(get("/api/v1/search?category=Electronics&sort=price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].name").value("Pulse Wireless Gaming Mouse")) // $79.99
                .andExpect(jsonPath("$.results[1].name").value("Pulse Mechanical Keyboard"))   // $129.99
                .andExpect(jsonPath("$.results[2].name").value("Pulse 32-inch 4K Monitor"));    // $499.99
    }

    @Test
    @DisplayName("Should asynchronously update search read projection when Kafka pricing and inventory events arrive")
    void testEventDrivenReadProjectionUpdateViaKafka() throws Exception {
        // Step 1: Simulate Pricing event PriceChanged for PROD-001 (Keyboard)
        String priceEventJson = """
                {
                  "eventId": "evt-price-update-01",
                  "eventType": "PriceChanged",
                  "payload": {
                    "productId": "PROD-001",
                    "price": "99.99"
                  }
                }
                """;
        ConsumerRecord<String, String> priceRecord = new ConsumerRecord<>(
                PulseCartTopics.PRICING_EVENTS, 0, 1L, "PROD-001", priceEventJson
        );
        eventListener.onPricingEvent(priceRecord);

        // Verify updated price in projection
        mockMvc.perform(get("/api/v1/search?q=keyboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].price").value(99.99));

        // Step 2: Simulate Inventory event StockDepleted for PROD-001
        String stockEventJson = """
                {
                  "eventId": "evt-stock-depleted-01",
                  "eventType": "StockDepleted",
                  "payload": {
                    "productId": "PROD-001",
                    "availableQuantity": 0
                  }
                }
                """;
        ConsumerRecord<String, String> stockRecord = new ConsumerRecord<>(
                PulseCartTopics.INVENTORY_EVENTS, 0, 2L, "PROD-001", stockEventJson
        );
        eventListener.onInventoryEvent(stockRecord);

        // Verify updated inStock status in projection
        mockMvc.perform(get("/api/v1/search?q=keyboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].inStock").value(false))
                .andExpect(jsonPath("$.results[0].stockQuantity").value(0));
    }
}
