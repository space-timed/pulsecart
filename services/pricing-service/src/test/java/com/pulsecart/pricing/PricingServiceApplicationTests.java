package com.pulsecart.pricing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.pricing.dto.PricingDtos.CalculatePriceRequest;
import com.pulsecart.pricing.dto.PricingDtos.SetPriceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PricingServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should assign price, record history, and calculate prices using different strategies")
    void shouldTestPricingStrategiesAndHistory() throws Exception {
        String productId = "prod-quantum-keyboard-101";

        // 1. Set Base Price
        SetPriceRequest setReq = new SetPriceRequest(
                productId,
                new BigDecimal("150.00"),
                "USD",
                "Product launch price",
                "pricing-admin"
        );

        mockMvc.perform(post("/api/v1/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(setReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.basePrice").value(150.00));

        // 2. Standard Strategy (1 item, standard tier)
        CalculatePriceRequest standardReq = new CalculatePriceRequest(
                productId, 1, "STANDARD", false, null
        );

        mockMvc.perform(post("/api/v1/prices/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedStrategy").value("STANDARD"))
                .andExpect(jsonPath("$.finalTotal").value(150.00))
                .andExpect(jsonPath("$.discountTotal").value(0.00));

        // 3. VIP Tier Strategy (GOLD = 15% discount)
        CalculatePriceRequest vipReq = new CalculatePriceRequest(
                productId, 1, "GOLD", false, null
        );

        mockMvc.perform(post("/api/v1/prices/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vipReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedStrategy").value("VIP_TIER"))
                .andExpect(jsonPath("$.subtotal").value(150.00))
                .andExpect(jsonPath("$.discountTotal").value(22.50))
                .andExpect(jsonPath("$.finalTotal").value(127.50));

        // 4. Volume Discount Strategy (10 items = 12% bulk discount)
        CalculatePriceRequest bulkReq = new CalculatePriceRequest(
                productId, 10, "STANDARD", false, null
        );

        mockMvc.perform(post("/api/v1/prices/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bulkReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedStrategy").value("VOLUME_DISCOUNT"))
                .andExpect(jsonPath("$.subtotal").value(1500.00))
                .andExpect(jsonPath("$.discountTotal").value(180.00))
                .andExpect(jsonPath("$.finalTotal").value(1320.00));

        // 5. Flash-Sale Strategy (takes precedence: 30% discount)
        CalculatePriceRequest flashReq = new CalculatePriceRequest(
                productId, 2, "GOLD", true, new BigDecimal("0.30")
        );

        mockMvc.perform(post("/api/v1/prices/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flashReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedStrategy").value("FLASH_SALE"))
                .andExpect(jsonPath("$.subtotal").value(300.00))
                .andExpect(jsonPath("$.discountTotal").value(90.00))
                .andExpect(jsonPath("$.finalTotal").value(210.00));

        // 6. Check price history audit trail
        mockMvc.perform(get("/api/v1/prices/" + productId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reason").value("Product launch price"));
    }
}
