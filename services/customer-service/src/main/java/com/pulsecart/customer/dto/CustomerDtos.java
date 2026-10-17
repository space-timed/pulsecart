package com.pulsecart.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public final class CustomerDtos {

    private CustomerDtos() {}

    public record CreateCustomerRequest(
            String keycloakId,
            @NotBlank(message = "Email is mandatory")
            @Email(message = "Email must be a valid format")
            String email,
            @NotBlank(message = "First name is mandatory")
            String firstName,
            @NotBlank(message = "Last name is mandatory")
            String lastName,
            String phoneNumber
    ) {}

    public record UpdateCustomerRequest(
            @NotBlank(message = "First name is mandatory")
            String firstName,
            @NotBlank(message = "Last name is mandatory")
            String lastName,
            String phoneNumber
    ) {}

    public record AddAddressRequest(
            @NotBlank(message = "Address line 1 is mandatory")
            String addressLine1,
            String addressLine2,
            @NotBlank(message = "City is mandatory")
            String city,
            @NotBlank(message = "State is mandatory")
            String state,
            @NotBlank(message = "Postal code is mandatory")
            String postalCode,
            @NotBlank(message = "Country is mandatory")
            String country,
            boolean isDefault
    ) {}

    public record AddressResponse(
            String id,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            String country,
            boolean isDefault,
            Instant createdAt
    ) {}

    public record CustomerResponse(
            String id,
            String keycloakId,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String status,
            Instant createdAt,
            Instant updatedAt,
            List<AddressResponse> addresses
    ) {}
}
