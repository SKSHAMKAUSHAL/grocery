/**
 * Repository interface for performing CRUD operations on the User entity.
 * Extends JpaRepository to inherit standard data access methods for the User entity with a String ID.
 */
package com.example.user.repository;

import com.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides data access methods for the User entity.
 * The String type parameter indicates that the User entity's ID is of type String.
 */
public interface UserRepository extends JpaRepository<User, String> {
}