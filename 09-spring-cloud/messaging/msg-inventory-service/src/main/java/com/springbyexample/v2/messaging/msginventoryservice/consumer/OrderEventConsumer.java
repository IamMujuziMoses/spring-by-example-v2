package com.springbyexample.v2.messaging.msginventoryservice.consumer;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.springbyexample.v2.messaging.msginventoryservice.event.OrderCreatedEvent;
import com.springbyexample.v2.messaging.msginventoryservice.service.InventoryService;

/**
 * Defines message consumers for inventory events.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class OrderEventConsumer {

    /**
     * Creates a consumer for order-created events.
     *
     * @param inventoryService the inventory service
     * @return the order-created event consumer
     */
    @Bean
    public Consumer<OrderCreatedEvent> orderCreated(InventoryService inventoryService) {
        return inventoryService::processOrder;
    }
}
