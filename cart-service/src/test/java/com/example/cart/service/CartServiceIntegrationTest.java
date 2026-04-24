package com.example.cart.service;

import com.example.cart.entity.CartItem;
import com.example.cart.entity.CartItemId;
import com.example.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = com.example.cart.Application.class)
@Testcontainers
@Transactional
public class CartServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("cartdb")
        .withUsername("postgres")
        .withPassword("password");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private org.springframework.web.client.RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll(); // Clear database
        CartService.Product product = new CartService.Product();
        product.setPrice(1.5);
        when(restTemplate.getForObject(anyString(), eq(CartService.Product.class))).thenReturn(product);
    }

    @Test
    void testAddToCart() {
        CartItem cartItem = new CartItem();
        CartItemId id = new CartItemId("testUser", 1L);
        cartItem.setId(id);
        cartItem.setQuantity(2);

        CartItem savedItem = cartService.addToCart(cartItem);
        assertNotNull(savedItem);
        assertEquals(2, savedItem.getQuantity());
        assertEquals(3.0, savedItem.getTotalPrice());

        List<CartItem> items = cartService.getCart("testUser");
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(3.0, items.get(0).getTotalPrice());
    }

    @Test
    void testGetCartEmpty() {
        List<CartItem> items = cartService.getCart("testUser");
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void testRemoveItem() {
        CartItem cartItem = new CartItem();
        CartItemId id = new CartItemId("testUser", 1L);
        cartItem.setId(id);
        cartItem.setQuantity(2);
        cartService.addToCart(cartItem);

        cartService.removeItem(id);
        List<CartItem> items = cartService.getCart("testUser");
        assertTrue(items.isEmpty());
    }

    @Test
    void testUpdateItem() {
        CartItem cartItem = new CartItem();
        CartItemId id = new CartItemId("testUser", 1L);
        cartItem.setId(id);
        cartItem.setQuantity(2);
        cartService.addToCart(cartItem);

        CartItem updatedItem = new CartItem();
        updatedItem.setId(id);
        updatedItem.setQuantity(3);
        CartItem result = cartService.updateItem(updatedItem);

        assertNotNull(result);
        assertEquals(3, result.getQuantity());
        assertEquals(4.5, result.getTotalPrice());
    }

    @Test
    void testClearCart() {
        CartItem cartItem1 = new CartItem();
        cartItem1.setId(new CartItemId("testUser", 1L));
        cartItem1.setQuantity(2);
        cartService.addToCart(cartItem1);

        CartItem cartItem2 = new CartItem();
        cartItem2.setId(new CartItemId("testUser", 2L));
        cartItem2.setQuantity(1);
        cartService.addToCart(cartItem2);

        cartService.clearCart("testUser");
        List<CartItem> items = cartService.getCart("testUser");
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }
}