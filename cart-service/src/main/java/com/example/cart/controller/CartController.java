package com.example.cart.controller;

import com.example.cart.entity.CartItem;
import com.example.cart.entity.CartItemId;
import com.example.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing cart operations such as adding, updating,
 * retrieving, and removing cart items for a specific user.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * Adds an item to the cart for a given user.
     */
    @Operation(summary = "Add item to cart")
    @PostMapping
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartItem cartItem, @RequestParam String userId) {
        logger.debug("Adding item to cart for userId: {}", userId);
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"User ID cannot be null or empty\"}");
        }
        cartItem.getId().setUserId(userId);
        CartItem savedItem = cartService.addToCart(cartItem);
        return savedItem != null ? ResponseEntity.status(201).body(savedItem)
                : ResponseEntity.status(404).body("{\"error\": \"Product not found\"}");
    }

    /**
     * Retrieves the list of items in the user's cart.
     */
    @Operation(summary = "View cart")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable String userId) {
        logger.debug("Fetching cart for userId: {}", userId);
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"User ID cannot be null or empty\"}");
        }
        List<CartItem> items = cartService.getCart(userId);
        return items != null ? ResponseEntity.ok(items.isEmpty() ? "{\"items\": []}" : items)
                : ResponseEntity.status(500).body("{\"error\": \"Failed to retrieve cart\"}");
    }

    /**
     * Removes an item from the user's cart based on userId and productId.
     */
    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/{userId}/{productId}")
    public ResponseEntity<?> removeItem(@PathVariable String userId, @PathVariable Long productId) {
        logger.debug("Removing item from cart for userId: {}, productId: {}", userId, productId);
        if (userId == null || userId.isEmpty() || productId == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"User ID or product ID cannot be null\"}");
        }
        CartItemId id = new CartItemId(userId, productId);
        if (!cartService.exists(id)) {
            return ResponseEntity.status(404).body("{\"error\": \"Item not found\"}");
        }
        cartService.removeItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates an item in the user's cart.
     */
    @Operation(summary = "Update item in cart")
    @PutMapping
    public ResponseEntity<?> updateItem(@Valid @RequestBody CartItem cartItem, @RequestParam String userId) {
        logger.debug("Updating item in cart for userId: {}", userId);
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"User ID cannot be null or empty\"}");
        }
        cartItem.getId().setUserId(userId);
        CartItem updatedItem = cartService.updateItem(cartItem);
        return updatedItem != null ? ResponseEntity.ok(updatedItem)
                : ResponseEntity.status(404).body("{\"error\": \"Product not found or item does not exist\"}");
    }

    /**
     * Clears all items from the user's cart.
     */
    @Operation(summary = "Clear cart")
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable String userId) {
        logger.debug("Clearing cart for userId: {}", userId);
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"User ID cannot be null or empty\"}");
        }
        cartService.clearCart(userId);
        return ResponseEntity.ok("{\"message\": \"Cart cleared\"}");
    }
}