package com.springbyexample.v2.loadbalancing.lborderclient.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.springbyexample.v2.loadbalancing.lborderclient.dto.OrderResponse;

/**
 * HTTP client for communicating with the order service.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderServiceClient {

    private final RestClient restClient;

    public OrderServiceClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://lb-order-service").build();
    }

    public OrderResponse getOrder(String orderId) {
        return restClient.get().uri("/orders/{orderId}", orderId).retrieve().body(OrderResponse.class);
    }

}
