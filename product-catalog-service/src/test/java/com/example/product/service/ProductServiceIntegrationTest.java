package com.example.product.service;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class ProductServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("productdb")
        .withUsername("postgres")
        .withPassword("password")
        .withReuse(true); // Optimize test performance

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll(); // Clear database before each test
    }

    @Test
    void testSaveAndGetProduct() {
        // Create and save a product
        Product product = new Product();
        product.setName("Apple");
        product.setDescription("Fresh red apple");
        product.setUnit("kg");
        product.setPrice(1.5);
        product.setShelfLifeDays(30);
        product.setStockQuantity(100);
        Product savedProduct = productService.saveProduct(product);

        // Retrieve and verify
        Product retrievedProduct = productService.getProductById(savedProduct.getId());
        assertNotNull(retrievedProduct);
        assertEquals("Apple", retrievedProduct.getName());
        assertEquals("Fresh red apple", retrievedProduct.getDescription());
        assertEquals("kg", retrievedProduct.getUnit());
        assertEquals(1.5, retrievedProduct.getPrice());
        assertEquals(30, retrievedProduct.getShelfLifeDays());
        assertEquals(100, retrievedProduct.getStockQuantity());
    }

    @Test
    void testGetProductNotFound() {
        Product retrievedProduct = productService.getProductById(999L);
        assertNull(retrievedProduct);
    }

    @Test
    void testGetAllProducts() {
        Product product1 = new Product();
        product1.setName("Apple");
        product1.setPrice(1.5);
        product1.setStockQuantity(100);
        productService.saveProduct(product1);

        Product product2 = new Product();
        product2.setName("Banana");
        product2.setPrice(2.0);
        product2.setStockQuantity(50);
        productService.saveProduct(product2);

        assertEquals(2, productService.getAllProducts().size());
    }
}