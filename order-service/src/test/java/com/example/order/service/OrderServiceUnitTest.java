package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderService orderService;

    private OrderService.CartItem cartItem;
    private OrderService.Product product;
    private Order order;

    @BeforeEach
    public void setUp() {
        // Setup cart item
        OrderService.CartItem.CartItemId cartItemId = new OrderService.CartItem.CartItemId();
        cartItemId.setUserId("testUser");
        cartItemId.setProductId(1L);
        cartItem = new OrderService.CartItem();
        cartItem.setId(cartItemId);
        cartItem.setQuantity(2);
        cartItem.setTotalPrice(20.0);

        // Setup product
        product = new OrderService.Product();
        product.setId(1L);
        product.setStockQuantity(10);

        // Setup order
        order = new Order();
        order.setId(1L);
        order.setUserId("testUser");
        order.setStatus("PLACED");
        Order.OrderItem orderItem = new Order.OrderItem(1L, 2, 20.0);
        order.setItems(Arrays.asList(orderItem));
        order.setTotalCost(20.0);
    }

    @Test
    public void testPlaceOrderSuccess() {
        // Mock cart-service response
        List<OrderService.CartItem> cartItems = Arrays.asList(cartItem);
        ResponseEntity<List<OrderService.CartItem>> cartResponse = new ResponseEntity<>(cartItems, HttpStatus.OK);
        when(restTemplate.exchange(
            eq("http://cart-service:8083/api/cart/testUser"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new org.springframework.core.ParameterizedTypeReference<List<OrderService.CartItem>>() {})
        )).thenReturn(cartResponse);

        // Mock product-service response
        ResponseEntity<OrderService.Product> productResponse = new ResponseEntity<>(product, HttpStatus.OK);
        when(restTemplate.exchange(
            eq("http://product-catalog-service:8082/api/products/1"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(OrderService.Product.class)
        )).thenReturn(productResponse);

        // Mock stock update response
        ResponseEntity<OrderService.Product> stockUpdateResponse = new ResponseEntity<>(product, HttpStatus.OK);
        when(restTemplate.exchange(
            eq("http://product-catalog-service:8082/api/products/1/stock"),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            eq(OrderService.Product.class)
        )).thenReturn(stockUpdateResponse);

        // Mock order save
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Mock cart clear
        doNothing().when(restTemplate).delete(eq("http://cart-service:8083/api/cart/clear/testUser"));

        // Execute
        Order result = orderService.placeOrder("testUser");

        // Verify
        assertNotNull(result);
        assertEquals("testUser", result.getUserId());
        assertEquals("PLACED", result.getStatus());
        assertEquals(20.0, result.getTotalCost());
        assertEquals(1, result.getItems().size());
        verify(restTemplate, times(1)).exchange(
            eq("http://cart-service:8083/api/cart/testUser"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new org.springframework.core.ParameterizedTypeReference<List<OrderService.CartItem>>() {})
        );
        verify(restTemplate, times(1)).exchange(
            eq("http://product-catalog-service:8082/api/products/1"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(OrderService.Product.class)
        );
        verify(restTemplate, times(1)).exchange(
            eq("http://product-catalog-service:8082/api/products/1/stock"),
            eq(HttpMethod.PUT),
            any(HttpEntity.class),
            eq(OrderService.Product.class)
        );
        verify(restTemplate, times(1)).delete(eq("http://cart-service:8083/api/cart/clear/testUser"));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testPlaceOrderEmptyCart() {
        // Mock empty cart response
        ResponseEntity<List<OrderService.CartItem>> cartResponse = new ResponseEntity<>(Arrays.asList(), HttpStatus.OK);
        when(restTemplate.exchange(
            eq("http://cart-service:8083/api/cart/testUser"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new org.springframework.core.ParameterizedTypeReference<List<OrderService.CartItem>>() {})
        )).thenReturn(cartResponse);

        // Execute and verify exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.placeOrder("testUser");
        });
        assertEquals("Cart is empty or an error occurred", exception.getMessage());
    }

    @Test
    public void testPlaceOrderProductNotFound() {
        // Mock cart response
        List<OrderService.CartItem> cartItems = Arrays.asList(cartItem);
        ResponseEntity<List<OrderService.CartItem>> cartResponse = new ResponseEntity<>(cartItems, HttpStatus.OK);
        when(restTemplate.exchange(
            eq("http://cart-service:8083/api/cart/testUser"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(new org.springframework.core.ParameterizedTypeReference<List<OrderService.CartItem>>() {})
        )).thenReturn(cartResponse);

        // Mock product-service returning null
        ResponseEntity<OrderService.Product> productResponse = null;
        when(restTemplate.exchange(
            eq("http://product-catalog-service:8082/api/products/1"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(OrderService.Product.class)
        )).thenReturn(productResponse);

        // Execute and verify exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            orderService.placeOrder("testUser");
        });
        assertEquals("Product fetch failed: 1", exception.getMessage());
    }
}