package com.pulsecart.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.inventory.dto.InventoryDtos.CreateInventoryRequest;
import com.pulsecart.inventory.dto.InventoryDtos.ReleaseStockRequest;
import com.pulsecart.inventory.dto.InventoryDtos.ReservationItemDto;
import com.pulsecart.inventory.dto.InventoryDtos.ReserveStockRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should initialize inventory, reserve stock, and execute compensating release")
    void shouldTestReservationAndCompensatingRelease() throws Exception {
        String productId = "prod-ergonomic-chair-55";

        // 1. Initialize
        CreateInventoryRequest initReq = new CreateInventoryRequest(
                productId, "SKU-CHAIR-55", 25
        );

        mockMvc.perform(post("/api/v1/inventory/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.availableQuantity").value(25))
                .andExpect(jsonPath("$.reservedQuantity").value(0));

        // 2. Reserve 5 units
        ReserveStockRequest reserveReq = new ReserveStockRequest(
                "ord-comp-test-1",
                List.of(new ReservationItemDto(productId, 5)),
                600
        );

        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reserveReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("ord-comp-test-1"))
                .andExpect(jsonPath("$.status").value("RESERVED"));

        // Verify quantities
        mockMvc.perform(get("/api/v1/inventory/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(20))
                .andExpect(jsonPath("$.reservedQuantity").value(5));

        // 3. Compensate (Release stock due to Saga compensation)
        ReleaseStockRequest releaseReq = new ReleaseStockRequest(
                "ord-comp-test-1", "Payment declined by customer bank"
        );

        mockMvc.perform(post("/api/v1/inventory/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(releaseReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));

        // Verify stock restored
        mockMvc.perform(get("/api/v1/inventory/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(25))
                .andExpect(jsonPath("$.reservedQuantity").value(0));
    }
}
