package com.pulsecart.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsecart.cart.dto.CartDtos.*;
import com.pulsecart.cart.model.Cart;
import com.pulsecart.cart.model.CartItem;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

@Service
public class CartService {

    private static final String CART_KEY_PREFIX = "pulsecart:cart:";
    private static final Duration CART_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public CartService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public CartResponse getCart(String customerId) {
        Cart cart = fetchCart(customerId);
        return toCartResponse(cart);
    }

    public CartResponse addItem(String customerId, AddCartItemRequest request) {
        Cart cart = fetchCart(customerId);
        CartItem newItem = new CartItem(
                request.productId(),
                request.sku(),
                request.productName(),
                request.quantity(),
                request.estimatedUnitPrice(),
                request.imageUrl()
        );
        cart.addItem(newItem);
        saveCart(cart);
        return toCartResponse(cart);
    }

    public CartResponse updateItemQuantity(String customerId, String productId, int quantity) {
        Cart cart = fetchCart(customerId);
        cart.updateQuantity(productId, quantity);
        saveCart(cart);
        return toCartResponse(cart);
    }

    public CartResponse removeItem(String customerId, String productId) {
        Cart cart = fetchCart(customerId);
        cart.removeItem(productId);
        saveCart(cart);
        return toCartResponse(cart);
    }

    public void clearCart(String customerId) {
        redisTemplate.delete(buildKey(customerId));
    }

    private Cart fetchCart(String customerId) {
        Object raw = redisTemplate.opsForValue().get(buildKey(customerId));
        if (raw instanceof Cart cart) {
            return cart;
        }
        if (raw != null) {
            try {
                return objectMapper.convertValue(raw, Cart.class);
            } catch (Exception ignored) {
            }
        }
        return new Cart(customerId);
    }

    private void saveCart(Cart cart) {
        String key = buildKey(cart.getCustomerId());
        redisTemplate.opsForValue().set(key, cart, CART_TTL);
    }

    private String buildKey(String customerId) {
        return CART_KEY_PREFIX + customerId;
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getProductId(),
                        i.getSku(),
                        i.getProductName(),
                        i.getQuantity(),
                        i.getEstimatedUnitPrice(),
                        i.getEstimatedUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())).setScale(2, RoundingMode.HALF_EVEN),
                        i.getImageUrl()
                ))
                .toList();

        int totalCount = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();

        return new CartResponse(
                cart.getCustomerId(),
                itemResponses,
                cart.getTotalEstimatedAmount(),
                totalCount,
                cart.getUpdatedAt()
        );
    }
}
