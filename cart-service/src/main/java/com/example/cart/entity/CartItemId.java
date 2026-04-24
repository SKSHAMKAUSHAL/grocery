package com.example.cart.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * Composite primary key for CartItem, consisting of userId and productId.
 * This class must implement Serializable and properly override equals and hashCode.
 */
@Embeddable
public class CartItemId implements Serializable {

    /**
     * ID of the user who owns the cart.
     */
    private String userId;

    /**
     * ID of the product added to the cart.
     */
    private Long productId;

    /**
     * Default no-args constructor required by JPA.
     */
    public CartItemId() {}

    /**
     * Parameterized constructor to initialize userId and productId.
     *
     * @param userId    the user's ID
     * @param productId the product's ID
     */
    public CartItemId(String userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    /**
     * Overrides equals to compare userId and productId.
     *
     * @param o the object to compare
     * @return true if both userId and productId match
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemId that = (CartItemId) o;
        return userId.equals(that.userId) && productId.equals(that.productId);
    }

    /**
     * Overrides hashCode using userId and productId.
     *
     * @return hash code for the composite key
     */
    @Override
    public int hashCode() {
        return 31 * userId.hashCode() + productId.hashCode();
    }

    /**
     * Gets the user ID.
     *
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId the user's ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the product ID.
     *
     * @return the productId
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * Sets the product ID.
     *
     * @param productId the product's ID
     */
    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
