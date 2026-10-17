package com.pulsecart.order.service;

import com.pulsecart.order.domain.Order;
import com.pulsecart.order.domain.SagaInstance;
import com.pulsecart.order.dto.OrderDtos.*;
import com.pulsecart.order.repository.OrderRepository;
import com.pulsecart.order.repository.SagaInstanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SagaInstanceRepository sagaRepository;

    public OrderService(OrderRepository orderRepository, SagaInstanceRepository sagaRepository) {
        this.orderRepository = orderRepository;
        this.sagaRepository = sagaRepository;
    }

    public Optional<OrderDetailResponse> getOrder(String orderId) {
        return orderRepository.findById(orderId).map(this::toOrderDetail);
    }

    public List<OrderDetailResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toOrderDetail)
                .toList();
    }

    public Optional<SagaInstanceDto> getSagaDetails(String orderId) {
        return sagaRepository.findByOrderId(orderId).map(s -> {
            List<SagaStepLogDto> steps = s.getStepLogs().stream()
                    .map(l -> new SagaStepLogDto(l.getStepName(), l.getStatus(), l.getPayload(), l.getErrorMessage(), l.getExecutedAt()))
                    .toList();

            return new SagaInstanceDto(
                    s.getId(),
                    s.getOrderId(),
                    s.getCurrentStep(),
                    s.getStatus(),
                    s.getCompensationReason(),
                    steps,
                    s.getStartedAt(),
                    s.getCompletedAt()
            );
        });
    }

    private OrderDetailResponse toOrderDetail(Order o) {
        List<OrderItemDto> itemDtos = o.getItems().stream()
                .map(i -> new OrderItemDto(i.getProductId(), i.getSku(), i.getProductName(), i.getUnitPrice(), i.getQuantity(), i.getSubtotal()))
                .toList();

        return new OrderDetailResponse(
                o.getId(),
                o.getCustomerId(),
                o.getStatus(),
                o.getSubtotal(),
                o.getDiscountAmount(),
                o.getTotalAmount(),
                o.getCurrency(),
                o.getReservationId(),
                o.getPaymentId(),
                o.getCancellationReason(),
                o.getShippingAddress(),
                itemDtos,
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
