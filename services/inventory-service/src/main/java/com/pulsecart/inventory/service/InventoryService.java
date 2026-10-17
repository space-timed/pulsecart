package com.pulsecart.inventory.service;

import com.pulsecart.inventory.domain.InventoryItem;
import com.pulsecart.inventory.domain.StockReservation;
import com.pulsecart.inventory.dto.InventoryDtos.*;
import com.pulsecart.inventory.repository.InventoryItemRepository;
import com.pulsecart.inventory.repository.StockReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository reservationRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository,
                            StockReservationRepository reservationRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.reservationRepository = reservationRepository;
    }

    public InventoryResponse initializeInventory(CreateInventoryRequest request) {
        if (inventoryItemRepository.findByProductId(request.productId()).isPresent()) {
            throw new IllegalArgumentException("Inventory already initialized for product: " + request.productId());
        }

        InventoryItem item = new InventoryItem(request.productId(), request.sku(), request.initialStock());
        InventoryItem saved = inventoryItemRepository.save(item);
        return toInventoryResponse(saved);
    }

    public InventoryResponse restock(String productId, RestockRequest request) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found for product ID: " + productId));

        item.restock(request.additionalQuantity());
        InventoryItem saved = inventoryItemRepository.save(item);
        return toInventoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(String productId) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found for product ID: " + productId));
        return toInventoryResponse(item);
    }

    /**
     * Executes atomic multi-item stock reservation.
     * Prevents overselling by utilizing conditional SQL updates backed by row-level locking.
     * If any single product lacks sufficient stock, all preceding reservations are immediately compensated.
     */
    public ReservationResponse reserveStock(ReserveStockRequest request) {
        int ttl = request.ttlSeconds() > 0 ? request.ttlSeconds() : 600; // default 10 minutes
        Instant expiresAt = Instant.now().plus(ttl, ChronoUnit.SECONDS);

        List<ReservationItemDto> successfullyReserved = new ArrayList<>();

        for (ReservationItemDto item : request.items()) {
            int updatedRows = inventoryItemRepository.reserveStockAtomic(item.productId(), item.quantity());
            if (updatedRows == 0) {
                // Compensate already reserved items in this transaction
                for (ReservationItemDto prev : successfullyReserved) {
                    inventoryItemRepository.releaseStockAtomic(prev.productId(), prev.quantity());
                }
                throw new IllegalStateException("Insufficient inventory to reserve product: " + item.productId() + " (requested: " + item.quantity() + ")");
            }
            successfullyReserved.add(item);
        }

        StockReservation reservation = new StockReservation(request.orderId(), expiresAt);
        for (ReservationItemDto item : successfullyReserved) {
            reservation.addItem(item.productId(), item.quantity());
        }

        StockReservation saved = reservationRepository.save(reservation);
        return toReservationResponse(saved);
    }

    /**
     * Compensating transaction: releases stock reservation back to available inventory.
     */
    public ReservationResponse releaseStock(ReleaseStockRequest request) {
        StockReservation reservation = reservationRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found for order: " + request.orderId()));

        if ("RELEASED".equals(reservation.getStatus())) {
            return toReservationResponse(reservation); // Idempotent success
        }

        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new IllegalStateException("Cannot release reservation in status: " + reservation.getStatus());
        }

        for (var item : reservation.getItems()) {
            inventoryItemRepository.releaseStockAtomic(item.getProductId(), item.getQuantity());
        }

        reservation.markReleased();
        StockReservation saved = reservationRepository.save(reservation);
        return toReservationResponse(saved);
    }

    /**
     * Confirms and permanently commits the reserved inventory upon successful payment capture.
     */
    public ReservationResponse commitStock(CommitStockRequest request) {
        StockReservation reservation = reservationRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found for order: " + request.orderId()));

        if ("CONFIRMED".equals(reservation.getStatus())) {
            return toReservationResponse(reservation); // Idempotent success
        }

        if (!"RESERVED".equals(reservation.getStatus())) {
            throw new IllegalStateException("Cannot commit reservation in status: " + reservation.getStatus());
        }

        for (var item : reservation.getItems()) {
            inventoryItemRepository.commitStockAtomic(item.getProductId(), item.getQuantity());
        }

        reservation.markConfirmed();
        StockReservation saved = reservationRepository.save(reservation);
        return toReservationResponse(saved);
    }

    private InventoryResponse toInventoryResponse(InventoryItem i) {
        return new InventoryResponse(
                i.getId(),
                i.getProductId(),
                i.getSku(),
                i.getAvailableQuantity(),
                i.getReservedQuantity(),
                i.getTotalQuantity(),
                i.getUpdatedAt()
        );
    }

    private ReservationResponse toReservationResponse(StockReservation r) {
        List<ReservationItemDto> itemDtos = r.getItems().stream()
                .map(item -> new ReservationItemDto(item.getProductId(), item.getQuantity()))
                .toList();

        return new ReservationResponse(
                r.getId(),
                r.getOrderId(),
                r.getStatus(),
                r.getExpiresAt(),
                itemDtos
        );
    }
}
