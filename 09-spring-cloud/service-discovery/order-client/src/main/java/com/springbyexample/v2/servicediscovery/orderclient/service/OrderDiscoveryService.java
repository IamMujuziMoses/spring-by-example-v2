package com.springbyexample.v2.servicediscovery.orderclient.service;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.springbyexample.v2.servicediscovery.orderclient.model.OrderResponse;

/**
 * Discovers the Order Service and invokes its HTTP endpoint.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderDiscoveryService {

    private static final String ORDER_SERVICE_ID = "order-service";

    private final DiscoveryClient discoveryClient;

    private final RestClient.Builder restClientBuilder;

    /**
     * Creates an Order Discovery Service.
     *
     * @param discoveryClient   service discovery client
     * @param restClientBuilder HTTP client builder
     */
    public OrderDiscoveryService(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
        this.discoveryClient = discoveryClient;
        this.restClientBuilder = restClientBuilder;
    }

    /**
     * Discovers an Order Service instance and retrieves an order.
     *
     * @param orderId order identifier
     * @return order response
     */
    public OrderResponse getOrder(String orderId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(ORDER_SERVICE_ID);

        if (instances.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No instances of order-service are currently available");
        }

        ServiceInstance instance = instances.getFirst();

        RestClient restClient = restClientBuilder.clone().baseUrl(instance.getUri().toString()).build();

        return restClient.get().uri("/api/orders/{orderId}", orderId).retrieve().body(OrderResponse.class);
    }
}
