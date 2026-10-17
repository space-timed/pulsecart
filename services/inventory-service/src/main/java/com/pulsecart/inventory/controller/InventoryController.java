package com.pulsecart.inventory.controller;

import com.pulsecart.inventory.dto.InventoryDtos.*;
import com.pulsecart.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/init")
    public ResponseEntity<InventoryResponse> initialize(@Valid @RequestBody CreateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.initializeInventory(request));
    }

    @PostMapping("/{productId}/restock")
    public ResponseEntity<InventoryResponse> restock(
            @PathVariable String productId,
            @Valid @RequestBody RestockRequest request
    ) {
        return ResponseEntity.ok(inventoryService.restock(productId, request));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReserveStockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.reserveStock(request));
    }

    @PostMapping("/release")
    public ResponseEntity<ReservationResponse> release(@Valid @RequestBody ReleaseStockRequest request) {
        return ResponseEntity.ok(inventoryService.releaseStock(request));
    }

    @PostMapping("/commit")
    public ResponseEntity<ReservationResponse> commit(@Valid @RequestBody CommitStockRequest request) {
        return ResponseEntity.ok(inventoryService.commitStock(request));
    }
}
