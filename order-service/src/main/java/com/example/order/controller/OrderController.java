package com.example.order.controller;

import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * REST controller for managing orders in the grocery ordering platform.
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final List<String> ALLOWED_STATUSES = Arrays.asList("PLACED", "SHIPPED", "DELIVERED", "CANCELLED");

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Place a new order for the authenticated user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order placed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or cart error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PostMapping
    public ResponseEntity<?> placeOrder() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            logger.error("{\"action\": \"place_order\", \"error\": \"Authentication required\"}");
            return new ResponseEntity<>("{\"error\": \"Authentication required\"}", HttpStatus.UNAUTHORIZED);
        }
        String userId = auth.getName();
        logger.info("{\"action\": \"place_order\", \"userId\": \"{}\"}", userId);
        try {
            Order order = orderService.placeOrder(userId);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("{\"action\": \"place_order\", \"error\": \"{}\", \"userId\": \"{}\"}", 
                e.getMessage(), userId);
            return new ResponseEntity<>("{\"error\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            logger.error("{\"action\": \"place_order\", \"error\": \"{}\", \"userId\": \"{}\"}", 
                e.getMessage(), userId);
            return new ResponseEntity<>("{\"error\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "View past orders for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "You are not authorized"),
        @ApiResponse(responseCode = "404", description = "No orders found for user"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping
    public ResponseEntity<?> getOrders(@RequestParam(required = false) String userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            logger.error("{\"action\": \"get_orders\", \"error\": \"Authentication required\"}");
            return new ResponseEntity<>("{\"error\": \"Authentication required\"}", HttpStatus.UNAUTHORIZED);
        }
        String currentUserId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        String targetUserId = isAdmin && userId != null && !userId.isBlank() ? userId : currentUserId;
        logger.info("{\"action\": \"get_orders\", \"userId\": \"{}\", \"targetUserId\": \"{}\"}", 
            currentUserId, targetUserId);
        if (!currentUserId.equals(targetUserId) && !isAdmin) {
            logger.warn("{\"action\": \"get_orders\", \"error\": \"Unauthorized attempt\", \"userId\": \"{}\"}", 
                currentUserId);
            return new ResponseEntity<>("{\"error\": \"You are not authorized to view other users' orders\"}", 
                HttpStatus.FORBIDDEN);
        }
        try {
            List<Order> orders = orderService.getOrders(targetUserId);
            return new ResponseEntity<>(orders.isEmpty() ? "{\"items\": []}" : orders, HttpStatus.OK);
        } catch (IllegalStateException e) {
            logger.error("{\"action\": \"get_orders\", \"error\": \"{}\", \"userId\": \"{}\"}", 
                e.getMessage(), targetUserId);
            return new ResponseEntity<>("{\"error\": \"No orders found for user\"}", HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Update order status (admin only)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "403", description = "You are not authorized"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam @NotBlank(message = "Status cannot be empty") String status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            logger.error("{\"action\": \"update_order_status\", \"error\": \"Authentication required\", \"orderId\": \"{}\"}", orderId);
            return new ResponseEntity<>("{\"error\": \"Authentication required\"}", HttpStatus.UNAUTHORIZED);
        }
        String userId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        String sanitizedStatus = status.trim().toUpperCase();
        logger.info("{\"action\": \"update_order_status\", \"orderId\": \"{}\", \"status\": \"{}\", \"userId\": \"{}\"}", 
            orderId, sanitizedStatus, userId);
        if (!isAdmin) {
            logger.warn("{\"action\": \"update_order_status\", \"error\": \"Unauthorized attempt\", \"userId\": \"{}\"}", 
                userId);
            return new ResponseEntity<>("{\"error\": \"You are not authorized\"}", HttpStatus.FORBIDDEN);
        }
        if (!ALLOWED_STATUSES.contains(sanitizedStatus)) {
            logger.error("{\"action\": \"update_order_status\", \"error\": \"Invalid status: {}\", \"orderId\": \"{}\"}", 
                sanitizedStatus, orderId);
            return new ResponseEntity<>(
                "{\"error\": \"Invalid status. Allowed: " + String.join(", ", ALLOWED_STATUSES) + "\"}", 
                HttpStatus.BAD_REQUEST);
        }
        try {
            Order order = orderService.updateOrderStatus(orderId, sanitizedStatus);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (IllegalStateException e) {
            logger.error("{\"action\": \"update_order_status\", \"error\": \"{}\", \"orderId\": \"{}\"}", 
                e.getMessage(), orderId);
            return new ResponseEntity<>("{\"error\": \"Order not found\"}", HttpStatus.NOT_FOUND);
        }
    }
}