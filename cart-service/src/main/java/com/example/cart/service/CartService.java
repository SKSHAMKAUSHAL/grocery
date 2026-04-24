package com.example.cart.service;

import com.example.cart.entity.CartItem;
import com.example.cart.entity.CartItemId;
import com.example.cart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service class for handling business logic related to the shopping cart.
 * Interacts with the CartRepository and external Product Catalog service.
 */
@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Adds a CartItem to the user's cart.
     * Fetches the product price from the product-catalog-service.
     *
     * @param cartItem the CartItem to be added
     * @return the saved CartItem or null if the product is not found or an error occurs
     */
    public CartItem addToCart(CartItem cartItem) {
        CartItemId id = cartItem.getId();

        if (id == null) {
            logger.error("CartItem ID cannot be null");
            throw new IllegalArgumentException("CartItem ID cannot be null");
        }

        if (id.getProductId() == null) {
            logger.error("Product ID cannot be null");
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        if (id.getUserId() == null || id.getUserId().isEmpty()) {
            logger.error("User ID cannot be null or empty");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        try {
            Double price = restTemplate.getForObject(
                "http://product-catalog-service:8082/api/products/" + id.getProductId(),
                Product.class
            ).getPrice();

            cartItem.setTotalPrice(cartItem.getQuantity() != null ? cartItem.getQuantity() * price : 0.0);

            logger.info("Adding cart item for userId: {}, productId: {}", id.getUserId(), id.getProductId());
            return cartRepository.save(cartItem);
        } catch (HttpClientErrorException e) {
            logger.error("Failed to fetch product price for productId: {}, status: {}", id.getProductId(), e.getStatusCode());
            return null;
        } catch (Exception e) {
            logger.error("Error adding cart item for userId: {}, productId: {}", id.getUserId(), id.getProductId(), e);
            return null;
        }
    }

    /**
     * Retrieves all CartItems for the given user.
     *
     * @param userId the ID of the user
     * @return list of CartItems or null if an error occurs
     */
    public List<CartItem> getCart(String userId) {
        try {
            logger.info("Fetching cart for userId: {}", userId);
            return cartRepository.findByIdUserId(userId);
        } catch (Exception e) {
            logger.error("Error fetching cart for userId: {}", userId, e);
            return null;
        }
    }

    /**
     * Checks if a CartItem exists for the given composite ID.
     *
     * @param id the composite key containing userId and productId
     * @return true if the item exists, false otherwise
     */
    public boolean exists(CartItemId id) {
        try {
            logger.debug("Checking if cart item exists for userId: {}, productId: {}", id.getUserId(), id.getProductId());
            return cartRepository.existsById(id);
        } catch (Exception e) {
            logger.error("Error checking existence of cart item for userId: {}, productId: {}", id.getUserId(), id.getProductId(), e);
            return false;
        }
    }

    /**
     * Removes a CartItem from the user's cart by composite ID.
     *
     * @param id the composite key containing userId and productId
     */
    public void removeItem(CartItemId id) {
        try {
            logger.info("Removing cart item for userId: {}, productId: {}", id.getUserId(), id.getProductId());
            cartRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("Error removing cart item for userId: {}, productId: {}", id.getUserId(), id.getProductId(), e);
        }
    }

    /**
     * Updates a CartItem in the user's cart.
     * Attempts to refetch the price from the product service; falls back to existing price if unavailable.
     *
     * @param cartItem the CartItem to update
     * @return the updated CartItem, or null if the item or price couldn't be resolved
     */
    public CartItem updateItem(CartItem cartItem) {
        CartItemId id = cartItem.getId();
        try {
            Double price = restTemplate.getForObject(
                "http://product-catalog-service:8082/api/products/" + id.getProductId(),
                Product.class
            ).getPrice();

            cartItem.setTotalPrice(cartItem.getQuantity() != null ? cartItem.getQuantity() * price : 0.0);

            logger.info("Updating cart item for userId: {}, productId: {}", id.getUserId(), id.getProductId());
            return cartRepository.save(cartItem);

        } catch (HttpClientErrorException e) {
            logger.error("Failed to fetch product price for productId: {}, status: {}", id.getProductId(), e.getStatusCode());

            CartItem existing = cartRepository.findById(id).orElse(null);
            if (existing != null) {
                cartItem.setTotalPrice(existing.getTotalPrice());
                return cartRepository.save(cartItem);
            }
            return null;

        } catch (Exception e) {
            logger.error("Error updating cart item for userId: {}, productId: {}", id.getUserId(), id.getProductId(), e);

            CartItem existing = cartRepository.findById(id).orElse(null);
            if (existing != null) {
                cartItem.setTotalPrice(existing.getTotalPrice());
                return cartRepository.save(cartItem);
            }
            return null;
        }
    }

    /**
     * Clears all CartItems for a specific user.
     *
     * @param userId the ID of the user whose cart should be cleared
     */
    @Transactional
    public void clearCart(String userId) {
        try {
            logger.info("Clearing cart for userId: {}", userId);
            cartRepository.deleteCartByUserId(userId);
            logger.info("Cart cleared successfully for userId: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to clear cart for userId: {}", userId, e);
            throw new RuntimeException("Failed to clear cart for userId: " + userId, e);
        }
    }

    /**
     * Inner static class used for mapping product responses from the product-catalog-service.
     */
    public static class Product {
        private Double price;

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }
}