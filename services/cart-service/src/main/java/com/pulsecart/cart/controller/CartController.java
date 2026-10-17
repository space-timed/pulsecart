package com.pulsecart.cart.controller;

import com.pulsecart.cart.dto.CartDtos.AddCartItemRequest;
import com.pulsecart.cart.dto.CartDtos.CartResponse;
import com.pulsecart.cart.dto.CartDtos.UpdateQuantityRequest;
import com.pulsecart.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/{customerId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable String customerId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItem(customerId, request));
    }

    @PutMapping("/{customerId}/items/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable String customerId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateQuantityRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItemQuantity(customerId, productId, request.quantity()));
    }

    @DeleteMapping("/{customerId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable String customerId,
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(cartService.removeItem(customerId, productId));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearCart(@PathVariable String customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
