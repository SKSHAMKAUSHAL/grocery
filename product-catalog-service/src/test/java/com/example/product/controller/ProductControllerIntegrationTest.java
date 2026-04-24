package com.example.product.controller;

import com.example.product.entity.Product;
import com.example.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("productdb")
            .withUsername("postgres")
            .withPassword("password")
            .withReuse(false) // Disable reuse for consistency
            .withStartupTimeout(Duration.ofMinutes(5)) // Increase timeout to 5 minutes
            .waitingFor(new HostPortWaitStrategy()); // Use port-based wait strategy

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService.getAllProducts().forEach(product -> productService.deleteProduct(product.getId())); // Clear database
    }

    @Test
    void testGetAllProducts() throws Exception {
        // Save a product
        Product product = new Product();
        product.setName("Apple");
        product.setPrice(1.5);
        product.setStockQuantity(100);
        productService.saveProduct(product);

        // Test GET /api/products
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apple"))
                .andExpect(jsonPath("$[0].price").value(1.5))
                .andExpect(jsonPath("$[0].stockQuantity").value(100));
    }

    @Test
    void testGetProductById() throws Exception {
        // Save a product
        Product product = new Product();
        product.setName("Apple");
        product.setPrice(1.5);
        product.setStockQuantity(100);
        Product savedProduct = productService.saveProduct(product);

        // Test GET /api/products/{id}
        mockMvc.perform(get("/api/products/" + savedProduct.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple"))
                .andExpect(jsonPath("$.price").value(1.5))
                .andExpect(jsonPath("$.stockQuantity").value(100));
    }

    @Test
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSaveProduct() throws Exception {
        // Test POST /api/products
        String productJson = """
            {
                "name": "Apple",
                "description": "Fresh red apple",
                "unit": "kg",
                "price": 1.5,
                "shelfLifeDays": 30,
                "stockQuantity": 100
            }
            """;
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apple"))
                .andExpect(jsonPath("$.price").value(1.5))
                .andExpect(jsonPath("$.stockQuantity").value(100));
    }
}