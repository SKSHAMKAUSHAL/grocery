package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class responsible for handling business logic related to Orders.
 */
@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order placeOrder(String userId) {
        logger.info("{\"action\": \"place_order\", \"userId\": \"{}\"}", userId);
        if (userId == null || userId.isBlank()) {
            logger.error("{\"action\": \"place_order\", \"error\": \"User ID cannot be null or empty\"}");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        ResponseEntity<List<CartItem>> cartResponse;
        try {
            cartResponse = restTemplate.exchange(
                "http://cart-service:8083/api/cart/" + userId,
                HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<List<CartItem>>() {}
            );
        } catch (HttpClientErrorException e) {
            logger.error("{\"action\": \"place_order\", \"error\": \"Failed to fetch cart\", \"userId\": \"{}\", \"status\": \"{}\"}",
                userId, e.getStatusCode());
            throw new IllegalStateException("Failed to fetch cart: " + e.getMessage());
        }
        List<CartItem> cartItems = cartResponse.getBody();
        if (cartResponse.getStatusCode() != HttpStatus.OK || cartItems == null || cartItems.isEmpty()) {
            logger.error("{\"action\": \"place_order\", \"error\": \"Cart is empty or an error occurred\", \"userId\": \"{}\"}", userId);
            throw new IllegalStateException("Cart is empty or an error occurred");
        }
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PLACED");
        double totalCost = 0.0;
        List<Order.OrderItem> orderItems = new ArrayList<>();
        Map<Long, Integer> updatedStock = new HashMap<>(); // For rollback
        // Get the Firebase ID token from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String firebaseToken = null;
        if (authentication != null && authentication.getCredentials() instanceof String) {
            firebaseToken = (String) authentication.getCredentials();
            logger.info("{\"action\": \"place_order\", \"userId\": \"{}\", \"firebaseToken\": \"{}\"}", userId, firebaseToken.length() > 10 ? firebaseToken.substring(0, 10) + "..." : firebaseToken);
        } else {
            logger.error("{\"action\": \"place_order\", \"userId\": \"{}\", \"error\": \"No Firebase token found in authentication context\", \"authCredentials\": \"{}\"}", 
                userId, authentication != null ? authentication.getCredentials() : "null");
            throw new IllegalStateException("No Firebase token available for stock update");
        }
        try {
            for (CartItem cartItem : cartItems) {
                if (cartItem.getId() == null || cartItem.getId().getProductId() == null ||
                    cartItem.getQuantity() == null || cartItem.getQuantity() <= 0 ||
                    cartItem.getTotalPrice() == null || cartItem.getTotalPrice() <= 0) {
                    logger.error("{\"action\": \"place_order\", \"error\": \"Invalid cart item\", \"userId\": \"{}\"}", userId);
                    throw new IllegalArgumentException("Invalid cart item: productId, quantity, or totalPrice is invalid");
                }
                ResponseEntity<Product> productResponse;
                try {
                    productResponse = restTemplate.getForEntity(
                        "http://product-catalog-service:8082/api/products/" + cartItem.getId().getProductId(),
                        Product.class
                    );
                } catch (HttpClientErrorException e) {
                    logger.error("{\"action\": \"place_order\", \"error\": \"Product not found\", \"productId\": \"{}\", \"status\": \"{}\"}",
                        cartItem.getId().getProductId(), e.getStatusCode());
                    throw new IllegalStateException("Product not found: " + cartItem.getId().getProductId());
                }
                Product product = productResponse.getBody();
                if (product == null || productResponse.getStatusCode() != HttpStatus.OK) {
                    logger.error("{\"action\": \"place_order\", \"error\": \"Product fetch failed\", \"productId\": \"{}\"}",
                        cartItem.getId().getProductId());
                    throw new IllegalStateException("Product fetch failed: " + cartItem.getId().getProductId());
                }
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    logger.error("{\"action\": \"place_order\", \"error\": \"Insufficient stock\", \"productId\": \"{}\"}",
                        cartItem.getId().getProductId());
                    throw new IllegalStateException("Insufficient stock for product: " + cartItem.getId().getProductId());
                }
                try {
                    // Add Firebase ID token to the stock update request
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", "Bearer " + firebaseToken);
                    logger.info("{\"action\": \"place_order\", \"userId\": \"{}\", \"productId\": \"{}\", \"stock_update\": \"Sending PUT with token\", \"token\": \"{}\"}", 
                        userId, cartItem.getId().getProductId(), firebaseToken.length() > 10 ? firebaseToken.substring(0, 10) + "..." : firebaseToken);
                    HttpEntity<StockUpdateRequest> requestEntity = new HttpEntity<>(new StockUpdateRequest(cartItem.getQuantity()), headers);
                    ResponseEntity<Product> stockResponse = restTemplate.exchange(
                        "http://product-catalog-service:8082/api/products/" + cartItem.getId().getProductId() + "/stock",
                        HttpMethod.PUT,
                        requestEntity,
                        Product.class
                    );
                    logger.info("{\"action\": \"place_order\", \"userId\": \"{}\", \"productId\": \"{}\", \"stock_update\": \"Success\", \"status\": \"{}\"}",
                        userId, cartItem.getId().getProductId(), stockResponse.getStatusCode());
                    updatedStock.put(cartItem.getId().getProductId(), cartItem.getQuantity());
                } catch (HttpClientErrorException e) {
                    logger.error("{\"action\": \"place_order\", \"error\": \"Failed to update stock\", \"productId\": \"{}\", \"status\": \"{}\"}",
                        cartItem.getId().getProductId(), e.getStatusCode());
                    throw new IllegalStateException("Failed to update stock for product: " + cartItem.getId().getProductId());
                }
                Order.OrderItem orderItem = new Order.OrderItem(
                    cartItem.getId().getProductId(),
                    cartItem.getQuantity(),
                    cartItem.getTotalPrice()
                );
                orderItems.add(orderItem);
                totalCost += cartItem.getTotalPrice();
            }
            order.setItems(orderItems);
            order.setTotalCost(totalCost);
            logger.info("{\"action\": \"place_order\", \"status\": \"saving\", \"userId\": \"{}\"}", userId);
            Order savedOrder = orderRepository.save(order);
            logger.info("{\"action\": \"place_order\", \"status\": \"clearing_cart\", \"userId\": \"{}\"}", userId);
            try {
                restTemplate.delete("http://cart-service:8083/api/cart/clear/" + userId);
            } catch (Exception e) {
                logger.error("{\"action\": \"place_order\", \"error\": \"Failed to clear cart\", \"userId\": \"{}\"}", userId);
                throw new IllegalStateException("Failed to clear cart");
            }
            logger.info("{\"action\": \"place_order\", \"status\": \"success\", \"userId\": \"{}\", \"orderId\": \"{}\"}",
                userId, savedOrder.getId());
            return savedOrder;
        } catch (Exception e) {
            logger.warn("{\"action\": \"place_order\", \"status\": \"rollback\", \"userId\": \"{}\"}", userId);
            for (Map.Entry<Long, Integer> entry : updatedStock.entrySet()) {
                try {
                    // Restore stock with Firebase ID token
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Authorization", "Bearer " + firebaseToken);
                    logger.info("{\"action\": \"place_order\", \"userId\": \"{}\", \"productId\": \"{}\", \"stock_restore\": \"Sending PUT with token\", \"token\": \"{}\"}", 
                        userId, entry.getKey(), firebaseToken.length() > 10 ? firebaseToken.substring(0, 10) + "..." : firebaseToken);
                    HttpEntity<StockUpdateRequest> requestEntity = new HttpEntity<>(new StockUpdateRequest(-entry.getValue()), headers);
                    ResponseEntity<Product> stockResponse = restTemplate.exchange(
                        "http://product-catalog-service:8082/api/products/" + entry.getKey() + "/stock",
                        HttpMethod.PUT,
                        requestEntity,
                        Product.class
                    );
                    logger.info("{\"action\": \"place_order\", \"status\": \"stock_restored\", \"productId\": \"{}\", \"status\": \"{}\"}",
                        entry.getKey(), stockResponse.getStatusCode());
                } catch (Exception restoreEx) {
                    logger.error("{\"action\": \"place_order\", \"error\": \"Failed to restore stock\", \"productId\": \"{}\"}",
                        entry.getKey());
                }
            }
            throw new IllegalStateException("Order placement failed: " + e.getMessage());
        }
    }

    public List<Order> getOrders(String userId) {
        logger.info("{\"action\": \"get_orders\", \"userId\": \"{}\"}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            logger.warn("{\"action\": \"get_orders\", \"error\": \"No orders found\", \"userId\": \"{}\"}", userId);
            throw new IllegalStateException("No orders found for user");
        }
        logger.info("{\"action\": \"get_orders\", \"status\": \"success\", \"userId\": \"{}\", \"count\": \"{}\"}",
            userId, orders.size());
        return orders;
    }

    public Order updateOrderStatus(Long orderId, String status) {
        logger.info("{\"action\": \"update_order_status\", \"orderId\": \"{}\", \"status\": \"{}\"}", orderId, status);
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            logger.error("{\"action\": \"update_order_status\", \"error\": \"Order not found\", \"orderId\": \"{}\"}", orderId);
            throw new IllegalStateException("Order not found");
        }
        Order order = orderOptional.get();
        order.setStatus(status);
        logger.info("{\"action\": \"update_order_status\", \"status\": \"success\", \"orderId\": \"{}\", \"newStatus\": \"{}\"}",
            orderId, status);
        return orderRepository.save(order);
    }

    public static class CartItem {
        private CartItemId id;
        private Integer quantity;
        private Double totalPrice;
        public CartItemId getId() { return id; }
        public void setId(CartItemId id) { this.id = id; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
        public static class CartItemId {
            private String userId;
            private Long productId;
            public String getUserId() { return userId; }
            public void setUserId(String userId) { this.userId = userId; }
            public Long getProductId() { return productId; }
            public void setProductId(Long productId) { this.productId = productId; }
        }
    }

    public static class Product {
        private Long id;
        private int stockQuantity;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public int getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    }

    public static class StockUpdateRequest {
        private int quantity;
        public StockUpdateRequest() {}
        public StockUpdateRequest(int quantity) { this.quantity = quantity; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}