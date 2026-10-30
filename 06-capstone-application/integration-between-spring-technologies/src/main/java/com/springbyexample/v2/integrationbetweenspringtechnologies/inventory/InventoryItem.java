package com.springbyexample.v2.integrationbetweenspringtechnologies.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Represents the inventory available for a product.
 *
 * @author Mujuzi Moses
 */
@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private int availableQuantity;

    @Version
    private Long version;

    protected InventoryItem() {
    }

    public InventoryItem(Long productId, int availableQuantity) {
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("Available quantity cannot be negative");
        }

        this.productId = productId;
        this.availableQuantity = availableQuantity;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reservation quantity must be greater than zero");
        }

        if (availableQuantity < quantity) {
            throw new InsufficientStockException(productId, quantity);
        }

        availableQuantity -= quantity;
    }
}
