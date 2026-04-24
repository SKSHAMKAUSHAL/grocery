/**
 * Service class for handling business logic related to User entities.
 * Provides methods for retrieving and saving user profiles.
 */
package com.example.user.service;

import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Manages user-related operations, interacting with the UserRepository for data access.
 */
@Service
public class UserService {
    /**
     * Repository dependency for performing CRUD operations on User entities.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return The User entity associated with the authenticated user's ID, or null if not found.
     */
    public User getUserProfile() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Saves a User entity to the database.
     *
     * @param user The User entity to save.
     * @return The saved User entity.
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}