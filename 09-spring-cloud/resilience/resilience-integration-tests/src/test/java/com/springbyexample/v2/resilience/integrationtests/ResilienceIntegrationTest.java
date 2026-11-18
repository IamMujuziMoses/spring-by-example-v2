package com.springbyexample.v2.resilience.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestClient;

import com.springbyexample.v2.resilience.orderclient.OrderClientApplication;
import com.springbyexample.v2.resilience.orderclient.OrderResponse;
import com.springbyexample.v2.resilience.orderservice.FailureModeService;
import com.springbyexample.v2.resilience.orderservice.OrderServiceApplication;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

/**
 * Integration tests for Resilience
 *
 * @author Mujuzi Moses
 */
@Execution(ExecutionMode.SAME_THREAD)
public class ResilienceIntegrationTest {

    private static final String ORDER_ID = "1001";

    private static ConfigurableApplicationContext orderServiceApplication;

    private static ConfigurableApplicationContext orderClientApplication;

    private static FailureModeService failureModeService;

    private static RestClient client;

    private static CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeAll
    static void startApplications() {
        orderServiceApplication = new SpringApplicationBuilder(OrderServiceApplication.class)
                .run(
                        "--server.port=0",
                        "--app.instance-id=order-service",
                        "--spring.main.banner-mode=off"
                );

        Integer orderServicePort = orderServiceApplication.getEnvironment()
                .getRequiredProperty("local.server.port", Integer.class);

        orderClientApplication = new SpringApplicationBuilder(OrderClientApplication.class)
                .run(
                        "--server.port=0",
                        "--app.order-service.base-url=http://localhost:" + orderServicePort,
                        "--spring.main.banner-mode=off"
                );

        Integer orderClientPort = orderClientApplication.getEnvironment()
                .getRequiredProperty("local.server.port", Integer.class);

        failureModeService = orderServiceApplication.getBean(FailureModeService.class);
        circuitBreakerRegistry = orderClientApplication.getBean(CircuitBreakerRegistry.class);
        client = RestClient.builder().baseUrl("http://localhost:" + orderClientPort).build();
    }

    @AfterAll
    static void stopApplications() {
        closeContext(orderClientApplication);
        closeContext(orderServiceApplication);
    }

    @BeforeEach
    void resetApplications() {
        failureModeService.reset();
        circuitBreakerRegistry.find("orderService").ifPresent(CircuitBreaker::reset);
    }

    @Test
    void shouldReturnSuccessfulOrderWhenServiceIsAvailable() {
        OrderResponse response = requestOrder();

        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.status()).isEqualTo("PROCESSED");
        assertThat(response.instanceId()).isEqualTo("order-service");
        assertThat(response.fallback()).isFalse();
        assertThat(failureModeService.getRequestCount()).isEqualTo(1);
    }

    @Test
    void shouldReturnFallbackWhenOrderServiceFails() {
        failureModeService.setFailureEnabled(true);

        OrderResponse response = requestOrder();

        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.status()).isEqualTo("UNAVAILABLE");
        assertThat(response.fallback()).isTrue();
        assertThat(response.message()).isEqualTo("Order service is temporarily unavailable");
        assertThat(failureModeService.getRequestCount()).isEqualTo(1);
    }

    @Test
    void shouldOpenCircuitAfterFailureThresholdIsReached() {
        failureModeService.setFailureEnabled(true);

        makeFailedRequests();

        assertThat(failureModeService.getRequestCount()).isEqualTo(4);

        OrderResponse fallbackResponse = requestOrder();

        assertThat(fallbackResponse.fallback()).isTrue();
        assertThat(failureModeService.getRequestCount()).isEqualTo(4);
    }

    @Test
    void shouldRecoverAfterOrderServiceBecomesAvailable() throws InterruptedException {
        failureModeService.setFailureEnabled(true);

        makeFailedRequests();

        assertThat(failureModeService.getRequestCount()).isEqualTo(4);

        failureModeService.setFailureEnabled(false);

        // Wait for the circuit breaker's open-state duration to expire.
        Thread.sleep(300);

        IntStream.range(0, 3)
                .forEach(index -> {
                    OrderResponse response = requestOrder();
                    assertThat(response.fallback()).isFalse();
                });

        assertThat(failureModeService.getRequestCount()).isEqualTo(7);
    }

    private static void makeFailedRequests() {
        IntStream.range(0, 4)
                .forEach(index -> {
                    OrderResponse response = requestOrder();
                    assertThat(response.fallback()).isTrue();
                });
    }

    private static OrderResponse requestOrder() {
        return client.get().uri("/client/orders/{orderId}", ResilienceIntegrationTest.ORDER_ID).retrieve()
                .body(OrderResponse.class);
    }

    private static void closeContext(ConfigurableApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }
}
