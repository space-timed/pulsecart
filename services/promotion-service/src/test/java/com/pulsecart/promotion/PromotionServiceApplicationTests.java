package com.pulsecart.promotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.promotion.domain.DiscountType;
import com.pulsecart.promotion.dto.PromotionDtos.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromotionServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should enforce specification rules, minimum order constraints, and max redemptions")
    void testCouponSpecificationAndAtomicRedemption() throws Exception {
        // 1. Create a coupon: FLASH20 (20% off, min $100, max discount $50, max 2 redemptions)
        CreateCouponRequest createReq = new CreateCouponRequest(
                "FLASH20",
                "Flash Sale 20% Off",
                DiscountType.PERCENTAGE,
                new BigDecimal("20.00"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                2,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(7, ChronoUnit.DAYS),
                null
        );

        mockMvc.perform(post("/api/v1/promotions/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("FLASH20"))
                .andExpect(jsonPath("$.maxRedemptions").value(2));

        // 2. Validate below min order amount ($60 < $100) -> should be invalid
        ValidateCouponRequest invalidAmountReq = new ValidateCouponRequest(
                "FLASH20",
                new BigDecimal("60.00"),
                "cust-1",
                "STANDARD"
        );

        mockMvc.perform(post("/api/v1/promotions/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAmountReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message", containsString("minimum order requirement")));

        // 3. Validate above min order amount ($200 >= $100) -> 20% of 200 = 40.00, final = 160.00
        ValidateCouponRequest validReq = new ValidateCouponRequest(
                "FLASH20",
                new BigDecimal("200.00"),
                "cust-1",
                "STANDARD"
        );

        mockMvc.perform(post("/api/v1/promotions/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.calculatedDiscount").value(40.00))
                .andExpect(jsonPath("$.finalOrderAmount").value(160.00));

        // 4. Apply 1st redemption -> success
        ApplyCouponRequest applyReq1 = new ApplyCouponRequest(
                "FLASH20",
                "ord-001",
                new BigDecimal("200.00"),
                "cust-1",
                "STANDARD"
        );

        mockMvc.perform(post("/api/v1/promotions/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.discountApplied").value(40.00));

        // 5. Apply 2nd redemption -> success (max redemptions reached)
        ApplyCouponRequest applyReq2 = new ApplyCouponRequest(
                "FLASH20",
                "ord-002",
                new BigDecimal("150.00"),
                "cust-2",
                "STANDARD"
        );

        mockMvc.perform(post("/api/v1/promotions/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.discountApplied").value(30.00));

        // 6. Apply 3rd redemption -> should be rejected because max redemptions (2) reached
        ApplyCouponRequest applyReq3 = new ApplyCouponRequest(
                "FLASH20",
                "ord-003",
                new BigDecimal("200.00"),
                "cust-3",
                "STANDARD"
        );

        mockMvc.perform(post("/api/v1/promotions/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyReq3)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("redemption limit")));
    }

    @Test
    @DisplayName("Should enforce VIP customer tier requirement")
    void testCustomerTierSpecification() throws Exception {
        CreateCouponRequest vipCoupon = new CreateCouponRequest(
                "VIPEXCLUSIVE",
                "VIP exclusive $50 discount",
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("50.00"),
                new BigDecimal("100.00"),
                null,
                100,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(7, ChronoUnit.DAYS),
                "VIP_PLATINUM"
        );

        mockMvc.perform(post("/api/v1/promotions/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vipCoupon)))
                .andExpect(status().isCreated());

        // Validate with STANDARD tier -> should fail
        ValidateCouponRequest standardUser = new ValidateCouponRequest(
                "VIPEXCLUSIVE",
                new BigDecimal("150.00"),
                "cust-norm",
                "STANDARD"
        );

        mockMvc.perform(post("/api/v1/promotions/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(standardUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message", containsString("requires customer tier VIP_PLATINUM")));

        // Validate with VIP_PLATINUM tier -> should succeed
        ValidateCouponRequest vipUser = new ValidateCouponRequest(
                "VIPEXCLUSIVE",
                new BigDecimal("150.00"),
                "cust-vip",
                "VIP_PLATINUM"
        );

        mockMvc.perform(post("/api/v1/promotions/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vipUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.calculatedDiscount").value(50.00))
                .andExpect(jsonPath("$.finalOrderAmount").value(100.00));
    }
}
