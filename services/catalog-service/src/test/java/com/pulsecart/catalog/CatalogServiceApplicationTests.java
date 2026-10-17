package com.pulsecart.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.catalog.domain.OutboxEvent;
import com.pulsecart.catalog.dto.CatalogDtos.CreateCategoryRequest;
import com.pulsecart.catalog.dto.CatalogDtos.CreateProductRequest;
import com.pulsecart.catalog.dto.CatalogDtos.SpecificationDto;
import com.pulsecart.catalog.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    @DisplayName("Should create category, product and verify Transactional Outbox event")
    void shouldCreateProductAndWriteOutboxEvent() throws Exception {
        // 1. Create Category
        CreateCategoryRequest categoryReq = new CreateCategoryRequest(
                "Electronics & Audio", "electronics-audio", "High-fidelity audio and gadgets", null
        );

        String catJson = mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String categoryId = objectMapper.readTree(catJson).get("id").asText();

        // 2. Create Product
        CreateProductRequest productReq = new CreateProductRequest(
                "SKU-PULSE-ANC-1",
                "Pulse Pro Wireless Headphones",
                "pulse-pro-wireless-headphones",
                "Industry-leading active noise cancellation with 40h battery.",
                categoryId,
                new BigDecimal("299.99"),
                "https://cdn.pulsecart.com/images/pulse-pro.jpg",
                "audio,wireless,anc,premium",
                List.of(
                        new SpecificationDto("Driver Size", "40mm"),
                        new SpecificationDto("Battery Life", "40 hours"),
                        new SpecificationDto("Bluetooth", "5.4")
                )
        );

        String prodJson = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.sku").value("SKU-PULSE-ANC-1"))
                .andExpect(jsonPath("$.basePrice").value(299.99))
                .andExpect(jsonPath("$.specifications", org.hamcrest.Matchers.hasSize(3)))
                .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(prodJson).get("id").asText();

        // 3. Verify Transactional Outbox atomic write
        List<OutboxEvent> outboxList = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        assertThat(outboxList).isNotEmpty();

        OutboxEvent productOutbox = outboxList.stream()
                .filter(e -> e.getAggregateId().equals(productId))
                .findFirst()
                .orElse(null);

        assertThat(productOutbox).isNotNull();
        assertThat(productOutbox.getEventType()).isEqualTo("catalog.product-created");
        assertThat(productOutbox.getPayload()).contains("SKU-PULSE-ANC-1");
        assertThat(productOutbox.getStatus()).isEqualTo("PENDING");

        // 4. Retrieve by SKU
        mockMvc.perform(get("/api/v1/products/by-sku/SKU-PULSE-ANC-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Pulse Pro Wireless Headphones"));
    }
}
