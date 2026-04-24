package com.example.order.entity;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entity representing an Order in the system.
 * <p>
 * This class maps to the "orders" table in the database and includes details
 * such as the user who placed the order, a list of order items, the total cost,
 * and the current status of the order.
 * </p>
 */
@Entity
@Table(name = "orders")
public class Order {

    /**
     * The unique identifier for the order.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifier of the user who placed the order.
     */
    private String userId;

    /**
     * List of items included in the order.
     * <p>
     * Mapped to the "order_items" table with a join column "order_id".
     * </p>
     */
    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items;

    /**
     * The total cost of the order.
     */
    private Double totalCost;

    /**
     * The status of the order.
     * Defaults to "PLACED".
     */
    private String status = "PLACED";

    /**
     * Gets the unique identifier of the order.
     *
     * @return the order ID
     */
    public Long getId() { 
        return id; 
    }

    /**
     * Sets the unique identifier of the order.
     *
     * @param id the order ID to set
     */
    public void setId(Long id) { 
        this.id = id; 
    }

    /**
     * Gets the user ID associated with the order.
     *
     * @return the user ID
     */
    public String getUserId() { 
        return userId; 
    }

    /**
     * Sets the user ID for the order.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) { 
        this.userId = userId; 
    }

    /**
     * Gets the list of order items.
     *
     * @return the list of order items
     */
    public List<OrderItem> getItems() { 
        return items; 
    }

    /**
     * Sets the list of order items.
     *
     * @param items the list of order items to set
     */
    public void setItems(List<OrderItem> items) { 
        this.items = items; 
    }

    /**
     * Gets the total cost of the order.
     *
     * @return the total cost
     */
    public Double getTotalCost() { 
        return totalCost; 
    }

    /**
     * Sets the total cost of the order.
     *
     * @param totalCost the total cost to set
     */
    public void setTotalCost(Double totalCost) { 
        this.totalCost = totalCost; 
    }

    /**
     * Gets the current status of the order.
     *
     * @return the order status
     */
    public String getStatus() { 
        return status; 
    }

    /**
     * Sets the status of the order.
     *
     * @param status the status to set
     */
    public void setStatus(String status) { 
        this.status = status; 
    }

    /**
     * Embeddable class representing an individual item within an order.
     * <p>
     * This class is stored as a collection in the "order_items" table and contains
     * details about the product, the quantity ordered, and the total price for that item.
     * </p>
     */
    @Embeddable
    public static class OrderItem {

        /**
         * Identifier for the product in the order item.
         */
        private Long productId;

        /**
         * Quantity of the product ordered.
         */
        private Integer quantity;

        /**
         * Total price for the ordered quantity of the product.
         */
        private Double totalPrice;

        /**
         * Default no-argument constructor.
         */
        public OrderItem() {}

        /**
         * Constructor with parameters to create an OrderItem.
         *
         * @param productId  the product ID
         * @param quantity   the quantity ordered
         * @param totalPrice the total price for the ordered quantity
         */
        public OrderItem(Long productId, Integer quantity, Double totalPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }

        /**
         * Gets the product ID.
         *
         * @return the product ID
         */
        public Long getProductId() { 
            return productId; 
        }

        /**
         * Sets the product ID.
         *
         * @param productId the product ID to set
         */
        public void setProductId(Long productId) { 
            this.productId = productId; 
        }

        /**
         * Gets the quantity of the product ordered.
         *
         * @return the quantity
         */
        public Integer getQuantity() { 
            return quantity; 
        }

        /**
         * Sets the quantity of the product ordered.
         *
         * @param quantity the quantity to set
         */
        public void setQuantity(Integer quantity) { 
            this.quantity = quantity; 
        }

        /**
         * Gets the total price for the order item.
         *
         * @return the total price
         */
        public Double getTotalPrice() { 
            return totalPrice; 
        }

        /**
         * Sets the total price for the order item.
         *
         * @param totalPrice the total price to set
         */
        public void setTotalPrice(Double totalPrice) { 
            this.totalPrice = totalPrice; 
        }
    }
}
