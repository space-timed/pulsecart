package com.pulsecart.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.cart.dto.CartDtos.AddCartItemRequest;
import com.pulsecart.cart.dto.CartDtos.UpdateQuantityRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should add items, merge quantities, update, and clear shopping cart in Redis")
    void testCompleteCartLifecycle() throws Exception {
        String customerId = "cust-cart-test-7788";

        // Clean previous test data if any
        mockMvc.perform(delete("/api/v1/cart/" + customerId))
                .andExpect(status().isNoContent());

        // 1. Initial cart should be empty
        mockMvc.perform(get("/api/v1/cart/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItemCount").value(0))
                .andExpect(jsonPath("$.totalEstimatedAmount").value(0.00));

        // 2. Add first item (qty: 2)
        AddCartItemRequest addReq1 = new AddCartItemRequest(
                "prod-headphones-1",
                "SKU-HP-1",
                "Pulse Pro Headphones",
                2,
                new BigDecimal("199.99"),
                "https://cdn.pulsecart.com/hp1.jpg"
        );

        mockMvc.perform(post("/api/v1/cart/" + customerId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalItemCount").value(2))
                .andExpect(jsonPath("$.totalEstimatedAmount").value(399.98));

        // 3. Add same item again (qty: 1) -> should merge quantity to 3
        AddCartItemRequest addReq2 = new AddCartItemRequest(
                "prod-headphones-1",
                "SKU-HP-1",
                "Pulse Pro Headphones",
                1,
                new BigDecimal("199.99"),
                "https://cdn.pulsecart.com/hp1.jpg"
        );

        mockMvc.perform(post("/api/v1/cart/" + customerId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalItemCount").value(3))
                .andExpect(jsonPath("$.totalEstimatedAmount").value(599.97));

        // 4. Update quantity to 5
        UpdateQuantityRequest updateReq = new UpdateQuantityRequest(5);
        mockMvc.perform(put("/api/v1/cart/" + customerId + "/items/prod-headphones-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItemCount").value(5))
                .andExpect(jsonPath("$.totalEstimatedAmount").value(999.95));

        // 5. Clear cart
        mockMvc.perform(delete("/api/v1/cart/" + customerId))
                .andExpect(status().isNoContent());

        // 6. Verify cart is empty again
        mockMvc.perform(get("/api/v1/cart/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItemCount").value(0));
    }
}
