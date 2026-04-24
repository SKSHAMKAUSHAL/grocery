package com.example.product.controller;

import com.example.product.entity.Product;
import com.example.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing product-related operations.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Retrieves a list of all products.
     *
     * @return ResponseEntity containing a list of all products with HTTP status 200 (OK).
     */
    @Operation(summary = "List all products")
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product to retrieve.
     * @return ResponseEntity containing the product if found with HTTP status 200 (OK),
     *         or HTTP status 404 (Not Found) with error message if no product exists.
     */
    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Creates a new product.
     *
     * @param product The product object to be saved, provided in the request body.
     * @return ResponseEntity containing the saved product with HTTP status 201 (Created),
     *         or HTTP status 400 (Bad Request) with error message if validation fails.
     */
    @Operation(summary = "Add a new product", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<?> saveProduct(@Valid @RequestBody Product product) {
        try {
            Product savedProduct = productService.saveProduct(product);
            return ResponseEntity.status(201).body(savedProduct);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Updates the stock quantity of a product identified by its ID.
     *
     * @param id      The ID of the product to update.
     * @param request The request body containing the new stock quantity.
     * @return ResponseEntity containing the updated product with HTTP status 200 (OK),
     *         or HTTP status 400 (Bad Request) with error message if the update fails.
     */
    @Operation(summary = "Update product stock quantity", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        try {
            Product updatedProduct = productService.updateStock(id, request.getQuantity());
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id The ID of the product to delete.
     * @return ResponseEntity with HTTP status 200 (OK) with success message,
     *         or HTTP status 404 (Not Found) with error message if the product does not exist.
     */
    @Operation(summary = "Delete a product by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Inner class representing the request body for updating product stock.
     */
    public static class StockUpdateRequest {
        @PositiveOrZero(message = "Quantity must be zero or positive")
        private int quantity;

        public StockUpdateRequest() {}

        public StockUpdateRequest(int quantity) {
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}