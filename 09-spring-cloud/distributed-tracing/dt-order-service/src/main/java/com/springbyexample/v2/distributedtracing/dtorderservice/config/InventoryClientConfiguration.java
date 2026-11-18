package com.springbyexample.v2.distributedtracing.dtorderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configures the HTTP client used to communicate with Inventory Service.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class InventoryClientConfiguration {

    @Bean
    public RestClient inventoryRestClient(RestClient.Builder builder,
            @Value("${inventory.service.url}") String inventoryServiceUrl) {
        return builder.baseUrl(inventoryServiceUrl).build();
    }
}

