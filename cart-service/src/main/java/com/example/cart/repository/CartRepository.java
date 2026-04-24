package com.example.cart.repository;

import com.example.cart.entity.CartItem;
import com.example.cart.entity.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Repository interface for managing CartItem entities using Spring Data JPA.
 * Provides methods for standard CRUD operations and custom queries on the cart.
 */
@Repository
public interface CartRepository extends JpaRepository<CartItem, CartItemId> {

    /**
     * Retrieves all cart items for a specific user.
     *
     * @param userId the ID of the user
     * @return list of CartItem entities associated with the user
     */
    List<CartItem> findByIdUserId(String userId);

    /**
     * Deletes all cart items for the given userId.
     * Uses a native SQL query to perform the operation directly in the database.
     *
     * @param userId the ID of the user whose cart items should be deleted
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cart_item WHERE user_id = ?1", nativeQuery = true)
    void deleteCartByUserId(String userId);
}
