/**
 * Entity class representing a User in the application.
 * This class is mapped to the "users" table in the database using JPA annotations.
 */
package com.example.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Defines the User entity with attributes for ID, email, and role.
 */
@Entity
@Table(name = "users")
public class User {
    /**
     * The unique identifier for the user.
     * This field is marked as the primary key for the entity.
     */
    @Id
    private String id;

    /**
     * The email address of the user.
     */
    private String email;

    /**
     * The role assigned to the user, defaults to "CONSUMER".
     */
    private String role = "CONSUMER";

    /**
     * Gets the unique identifier of the user.
     *
     * @return The user's ID.
     */
    public String getId() { return id; }

    /**
     * Sets the unique identifier of the user.
     *
     * @param id The ID to set for the user.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Gets the email address of the user.
     *
     * @return The user's email address.
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address of the user.
     *
     * @param email The email address to set for the user.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the role of the user.
     *
     * @return The user's role.
     */
    public String getRole() { return role; }

    /**
     * Sets the role of the user.
     *
     * @param role The role to set for the user.
     */
    public void setRole(String role) { this.role = role; }
}