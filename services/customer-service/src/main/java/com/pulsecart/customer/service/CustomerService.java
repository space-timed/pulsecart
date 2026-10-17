package com.pulsecart.customer.service;

import com.pulsecart.customer.domain.Customer;
import com.pulsecart.customer.domain.CustomerAddress;
import com.pulsecart.customer.dto.CustomerDtos.*;
import com.pulsecart.customer.exception.CustomerNotFoundException;
import com.pulsecart.customer.repository.CustomerAddressRepository;
import com.pulsecart.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;

    public CustomerService(CustomerRepository customerRepository, CustomerAddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Customer with email '" + request.email() + "' already exists");
        }

        Customer customer = new Customer(
                request.keycloakId(),
                request.email(),
                request.firstName(),
                request.lastName(),
                request.phoneNumber()
        );

        Customer saved = customerRepository.save(customer);
        return toCustomerResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        return toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
        return toCustomerResponse(customer);
    }

    public CustomerResponse updateProfile(String id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        customer.updateProfile(request.firstName(), request.lastName(), request.phoneNumber());
        return toCustomerResponse(customer);
    }

    public AddressResponse addAddress(String customerId, AddAddressRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        if (request.isDefault()) {
            List<CustomerAddress> existing = addressRepository.findByCustomerId(customerId);
            for (CustomerAddress addr : existing) {
                addr.setDefault(false);
            }
        }

        CustomerAddress address = new CustomerAddress(
                customer,
                request.addressLine1(),
                request.addressLine2(),
                request.city(),
                request.state(),
                request.postalCode(),
                request.country(),
                request.isDefault()
        );

        customer.addAddress(address);
        customerRepository.save(customer);
        return toAddressResponse(address);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getCustomerAddresses(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
        return addressRepository.findByCustomerId(customerId).stream()
                .map(this::toAddressResponse)
                .toList();
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        List<AddressResponse> addresses = customer.getAddresses().stream()
                .map(this::toAddressResponse)
                .toList();

        return new CustomerResponse(
                customer.getId(),
                customer.getKeycloakId(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber(),
                customer.getStatus().name(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                addresses
        );
    }

    private AddressResponse toAddressResponse(CustomerAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.isDefault(),
                address.getCreatedAt()
        );
    }
}
