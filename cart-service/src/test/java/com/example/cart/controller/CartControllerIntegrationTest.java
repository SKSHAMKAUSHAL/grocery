package com.example.cart.controller;

import com.example.cart.entity.CartItem;
import com.example.cart.entity.CartItemId;
import com.example.cart.repository.CartRepository;
import com.example.cart.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.example.cart.Application.class)
@Testcontainers
@AutoConfigureMockMvc
public class CartControllerIntegrationTest {

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
    private MockMvc mockMvc;

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
    void testAddToCart() throws Exception {
        String cartItemJson = """
            {
                "id": {"userId": "testUser", "productId": 1},
                "quantity": 2
            }
            """;
        mockMvc.perform(post("/api/cart?userId=testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cartItemJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.quantity").value(2))
            .andExpect(jsonPath("$.totalPrice").value(3.0));
    }

    @Test
    void testGetCart() throws Exception {
        CartItem cartItem = new CartItem();
        cartItem.setId(new CartItemId("testUser", 1L));
        cartItem.setQuantity(2);
        cartService.addToCart(cartItem);

        mockMvc.perform(get("/api/cart/testUser")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].quantity").value(2))
            .andExpect(jsonPath("$[0].totalPrice").value(3.0));
    }

    @Test
    void testGetCartEmpty() throws Exception {
        mockMvc.perform(get("/api/cart/testUser")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"message\": \"Cart is empty\"}"));
    }

    @Test
    void testRemoveItem() throws Exception {
        CartItem cartItem = new CartItem();
        cartItem.setId(new CartItemId("testUser", 1L));
        cartItem.setQuantity(2);
        cartService.addToCart(cartItem);

        mockMvc.perform(delete("/api/cart/testUser/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateItem() throws Exception {
        CartItem cartItem = new CartItem();
        cartItem.setId(new CartItemId("testUser", 1L));
        cartItem.setQuantity(2);
        cartService.addToCart(cartItem);

        String updatedCartItemJson = """
            {
                "id": {"userId": "testUser", "productId": 1},
                "quantity": 3
            }
            """;
        mockMvc.perform(put("/api/cart?userId=testUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedCartItemJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(3))
            .andExpect(jsonPath("$.totalPrice").value(4.5));
    }

    @Test
    void testClearCart() throws Exception {
        CartItem cartItem = new CartItem();
        cartItem.setId(new CartItemId("testUser", 1L));
        cartItem.setQuantity(2);
        cartService.addToCart(cartItem);

        mockMvc.perform(delete("/api/cart/clear/testUser")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }
}