package com.springbyexample.v2.integrationbetweenspringtechnologies.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.Product;
import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.ProductRepository;
import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InventoryItem;
import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InventoryRepository;

/**
 * Initializes the application with sample products and inventory.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(ProductRepository productRepository, InventoryRepository inventoryRepository) {

        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            Product laptop = productRepository.save(new Product("Laptop", new BigDecimal("1200.00")));

            Product keyboard = productRepository.save(new Product("Keyboard", new BigDecimal("75.00")));

            Product mouse = productRepository.save(new Product("Mouse", new BigDecimal("25.00")));

            inventoryRepository.save(new InventoryItem(laptop.getId(), 10));

            inventoryRepository.save(new InventoryItem(keyboard.getId(), 25));

            inventoryRepository.save(new InventoryItem(mouse.getId(), 50));
        };
    }
}
