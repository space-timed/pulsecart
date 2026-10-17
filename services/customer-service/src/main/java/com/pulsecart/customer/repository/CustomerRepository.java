package com.pulsecart.customer.repository;

import com.pulsecart.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByKeycloakId(String keycloakId);
    boolean existsByEmail(String email);
}
