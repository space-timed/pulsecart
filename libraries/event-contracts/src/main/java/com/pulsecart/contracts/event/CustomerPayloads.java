package com.pulsecart.contracts.event;

import java.math.BigDecimal;
import java.util.List;

public final class CustomerPayloads {
    private CustomerPayloads() {}

    public record CustomerCreatedPayload(
            String customerId,
            String keycloakId,
            String email,
            String firstName,
            String lastName
    ) {}
}
