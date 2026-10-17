package com.pulsecart.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.customer.dto.CustomerDtos.AddAddressRequest;
import com.pulsecart.customer.dto.CustomerDtos.CreateCustomerRequest;
import com.pulsecart.customer.dto.CustomerDtos.UpdateCustomerRequest;
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
class CustomerServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create customer profile and retrieve by ID and email")
    void shouldCreateAndRetrieveCustomer() throws Exception {
        CreateCustomerRequest createReq = new CreateCustomerRequest(
                "keycloak-user-001",
                "alex.mercer@pulsecart.com",
                "Alex",
                "Mercer",
                "+1-555-0199"
        );

        String responseJson = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("alex.mercer@pulsecart.com"))
                .andExpect(jsonPath("$.firstName").value("Alex"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        String customerId = objectMapper.readTree(responseJson).get("id").asText();

        // Retrieve by ID
        mockMvc.perform(get("/api/v1/customers/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.lastName").value("Mercer"));

        // Retrieve by email
        mockMvc.perform(get("/api/v1/customers/by-email").param("email", "alex.mercer@pulsecart.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId));
    }

    @Test
    @DisplayName("Should update customer profile information")
    void shouldUpdateCustomerProfile() throws Exception {
        CreateCustomerRequest createReq = new CreateCustomerRequest(
                "keycloak-user-002",
                "sara.connor@pulsecart.com",
                "Sara",
                "Connor",
                "+1-555-0288"
        );

        String responseJson = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String customerId = objectMapper.readTree(responseJson).get("id").asText();

        UpdateCustomerRequest updateReq = new UpdateCustomerRequest(
                "Sarah",
                "Connor-Reese",
                "+1-555-9999"
        );

        mockMvc.perform(put("/api/v1/customers/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Sarah"))
                .andExpect(jsonPath("$.lastName").value("Connor-Reese"))
                .andExpect(jsonPath("$.phoneNumber").value("+1-555-9999"));
    }

    @Test
    @DisplayName("Should add address to customer and list addresses")
    void shouldAddAndListAddresses() throws Exception {
        CreateCustomerRequest createReq = new CreateCustomerRequest(
                "keycloak-user-003",
                "elena.rostova@pulsecart.com",
                "Elena",
                "Rostova",
                "+1-555-0377"
        );

        String responseJson = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String customerId = objectMapper.readTree(responseJson).get("id").asText();

        AddAddressRequest addressReq = new AddAddressRequest(
                "742 Evergreen Terrace",
                "Apt 4B",
                "Springfield",
                "Oregon",
                "97477",
                "USA",
                true
        );

        mockMvc.perform(post("/api/v1/customers/" + customerId + "/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.city").value("Springfield"))
                .andExpect(jsonPath("$.isDefault").value(true));

        mockMvc.perform(get("/api/v1/customers/" + customerId + "/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].addressLine1").value("742 Evergreen Terrace"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request on invalid customer email")
    void shouldRejectInvalidEmail() throws Exception {
        CreateCustomerRequest invalidReq = new CreateCustomerRequest(
                null,
                "not-an-email",
                "Bad",
                "User",
                null
        );

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.email").isNotEmpty());
    }
}
