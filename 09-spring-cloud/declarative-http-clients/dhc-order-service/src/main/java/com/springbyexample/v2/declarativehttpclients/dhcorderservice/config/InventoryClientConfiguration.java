package com.springbyexample.v2.declarativehttpclients.dhcorderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.springbyexample.v2.declarativehttpclients.dhcorderservice.client.InventoryClient;

/**
 * Configuration for declarative HTTP clients.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class InventoryClientConfiguration {

    /**
     * Creates the Inventory Service HTTP client.
     *
     * @param builder             the configured RestClient builder
     * @param inventoryServiceUrl the Inventory Service base URL
     * @return the declarative InventoryClient proxy
     */
    @Bean
    public InventoryClient inventoryClient(RestClient.Builder builder,
            @Value("${inventory.service.url}") String inventoryServiceUrl) {

        RestClient restClient = builder.baseUrl(inventoryServiceUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(InventoryClient.class);
    }
}
