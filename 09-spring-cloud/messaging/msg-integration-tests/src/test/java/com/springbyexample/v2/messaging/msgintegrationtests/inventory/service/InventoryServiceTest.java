package com.springbyexample.v2.messaging.msgintegrationtests.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.messaging.msginventoryservice.event.OrderCreatedEvent;
import com.springbyexample.v2.messaging.msginventoryservice.model.InventoryItem;
import com.springbyexample.v2.messaging.msginventoryservice.service.InventoryService;

/**
 * Unit tests for {@link InventoryService}.
 *
 * @author Mujuzi Moses
 */
public class InventoryServiceTest {

    private static final String PRODUCT_ID = "SKU-1001";

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
    }

    @Test
    void shouldFindExistingInventoryItem() {
        Optional<InventoryItem> result = inventoryService.findByProductId(PRODUCT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().productId()).isEqualTo(PRODUCT_ID);
        assertThat(result.get().availableQuantity()).isEqualTo(25);
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        Optional<InventoryItem> result = inventoryService.findByProductId("SKU-UNKNOWN");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReduceInventoryWhenOrderIsProcessed() {
        inventoryService.processOrder(createOrderEvent(5));

        Optional<InventoryItem> result = inventoryService.findByProductId(PRODUCT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().availableQuantity()).isEqualTo(20);
    }

    @Test
    void shouldNotAllowInventoryToBecomeNegative() {
        inventoryService.processOrder(createOrderEvent(30));

        Optional<InventoryItem> result = inventoryService.findByProductId(PRODUCT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().availableQuantity()).isZero();
    }

    @Test
    void shouldNotChangeInventoryForUnknownProduct() {
        OrderCreatedEvent event = new OrderCreatedEvent("event-001", "order-001", "SKU-UNKNOWN",
                5, Instant.now());

        inventoryService.processOrder(event);

        assertThat(inventoryService.findByProductId(PRODUCT_ID)).isPresent().get()
                .extracting(InventoryItem::availableQuantity).isEqualTo(25);
    }

    private OrderCreatedEvent createOrderEvent(int quantity) {
        return new OrderCreatedEvent("event-001", "order-001", PRODUCT_ID, quantity, Instant.now());
    }
}
