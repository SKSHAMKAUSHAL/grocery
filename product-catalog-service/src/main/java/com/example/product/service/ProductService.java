package com.example.product.service;

import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for handling business logic related to {@link Product} operations.
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Retrieves all available products from the repository.
     *
     * @return a list of all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product
     * @return the product if found
     * @throws IllegalStateException if the product is not found
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product not found"));
    }

    /**
     * Saves a new product or updates an existing product in the repository.
     *
     * @param product the product to save
     * @return the saved product
     * @throws IllegalStateException if a product with the same name already exists
     */
    public Product saveProduct(Product product) {
        if (productRepository.findByName(product.getName()).isPresent()) {
            throw new IllegalStateException("Product name already exists");
        }
        return productRepository.save(product);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the ID of the product to delete
     * @throws IllegalStateException if the product is not found
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalStateException("Product not found");
        }
        productRepository.deleteById(id);
    }

    /**
     * Updates the stock quantity of a product to the specified quantity.
     *
     * @param productId the ID of the product to update
     * @param quantity the new stock quantity
     * @return the updated product
     * @throws IllegalStateException if the product is not found
     * @throws IllegalArgumentException if the quantity is negative
     */
    public Product updateStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalStateException("Product not found"));
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        product.setStockQuantity(quantity);
        return productRepository.save(product);
    }
}