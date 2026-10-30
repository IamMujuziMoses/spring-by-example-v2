package com.springbyexample.v2.integrationbetweenspringtechnologies.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides the public operations for the Inventory module.
 *
 * @author Mujuzi Moses
 */
@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public int getAvailableQuantity(Long productId) {
        return inventoryRepository.findByProductId(productId).map(InventoryItem::getAvailableQuantity).orElse(0);
    }

    @Transactional
    public void reserveStock(Long productId, int quantity) {
        InventoryItem inventory = inventoryRepository.findByProductIdForUpdate(productId).orElseThrow(() ->
                new InsufficientStockException(productId, quantity));

        inventory.reserve(quantity);
    }
}
