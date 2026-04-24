package com.example.order.repository;

import com.example.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface for accessing Order entities from the database.
 * <p>
 * Extends {@link JpaRepository} to provide basic CRUD operations.
 * Includes a custom query method to retrieve orders by user ID.
 * </p>
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Retrieves all orders placed by a specific user.
     *
     * @param userId the ID of the user
     * @return a list of orders placed by the specified user
     */
    List<Order> findByUserId(String userId);
}
