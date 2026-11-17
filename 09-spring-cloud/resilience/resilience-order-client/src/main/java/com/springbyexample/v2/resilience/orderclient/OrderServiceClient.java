package com.springbyexample.v2.resilience.orderclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * @author Mujuzi Moses
 */
@Component
public class OrderServiceClient {

    private static final String CIRCUIT_BREAKER_ID = "orderService";

    private final RestClient restClient;

    private final org.springframework.cloud.client.circuitbreaker.CircuitBreaker circuitBreaker;

    public OrderServiceClient(RestClient.Builder restClientBuilder, CircuitBreakerFactory<?, ?> circuitBreakerFactory,
            @Value("${app.order-service.base-url}") String orderServiceBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(orderServiceBaseUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
    }

    public OrderResponse getOrder(String orderId) {
        return circuitBreaker.run(
                () -> restClient.get().uri("/orders/{orderId}", orderId).retrieve().body(OrderResponse.class),
                throwable -> fallback(orderId));
    }

    private OrderResponse fallback(String orderId) {
        return new OrderResponse(orderId, "UNAVAILABLE", null, true,
                "Order service is temporarily unavailable");
    }

}
