package com.pulsecart.order.controller;

import com.pulsecart.order.dto.OrderDtos.*;
import com.pulsecart.order.saga.OrderSagaCoordinator;
import com.pulsecart.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderSagaCoordinator sagaCoordinator;
    private final OrderService orderService;

    public OrderController(OrderSagaCoordinator sagaCoordinator, OrderService orderService) {
        this.sagaCoordinator = sagaCoordinator;
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Chaos-Payment-Action", required = false) String headerChaosAction,
            @Valid @RequestBody CheckoutRequest request) {

        String effectiveChaos = request.chaosPaymentAction() != null ? request.chaosPaymentAction() : headerChaosAction;
        CheckoutRequest effectiveReq = new CheckoutRequest(
                request.customerId(),
                request.items(),
                request.shippingAddress(),
                request.paymentMethodToken(),
                request.promoCode(),
                effectiveChaos
        );

        CheckoutResponse response = sagaCoordinator.executeSaga(effectiveReq, idempotencyKey);
        if (response.status() == com.pulsecart.order.domain.OrderStatus.CANCELLED) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/orders/customer/{customerId}")
    public ResponseEntity<List<OrderDetailResponse>> getOrdersByCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/orders/{orderId}/saga")
    public ResponseEntity<SagaInstanceDto> getSagaDetails(@PathVariable String orderId) {
        return orderService.getSagaDetails(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
