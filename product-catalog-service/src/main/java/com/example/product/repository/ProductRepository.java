package com.example.product.repository;

import com.example.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Product} entities.
 * Extends {@link JpaRepository} to provide standard CRUD operations.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Deletes the product with the specified ID.
     *
     * @param id the ID of the product to delete
     */
    void deleteById(Long id);

    /**
     * Finds a product by its name.
     *
     * @param name the name of the product
     * @return an Optional containing the product if found, or empty if not
     */
    Optional<Product> findByName(String name);
}