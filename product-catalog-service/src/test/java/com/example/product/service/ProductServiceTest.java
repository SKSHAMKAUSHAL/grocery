package com.example.product.service;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Apple");
        product.setDescription("Fresh red apple");
        product.setUnit("kg");
        product.setPrice(1.5);
        product.setShelfLifeDays(30);
        product.setStockQuantity(100);
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        List<Product> products = productService.getAllProducts();
        assertEquals(1, products.size());
        assertEquals("Apple", products.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Product retrievedProduct = productService.getProductById(1L);
        assertNotNull(retrievedProduct);
        assertEquals("Apple", retrievedProduct.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        Product retrievedProduct = productService.getProductById(999L);
        assertNull(retrievedProduct);
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void testSaveProduct() {
        when(productRepository.save(product)).thenReturn(product);
        Product savedProduct = productService.saveProduct(product);
        assertNotNull(savedProduct);
        assertEquals("Apple", savedProduct.getName());
        verify(productRepository, times(1)).save(product);
    }
}