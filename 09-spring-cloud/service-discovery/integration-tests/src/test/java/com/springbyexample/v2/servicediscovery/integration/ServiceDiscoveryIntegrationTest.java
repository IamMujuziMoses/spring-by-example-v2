package com.springbyexample.v2.servicediscovery.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestClient;

import com.springbyexample.v2.servicediscovery.discoveryserver.DiscoveryServerApplication;
import com.springbyexample.v2.servicediscovery.orderclient.OrderClientApplication;
import com.springbyexample.v2.servicediscovery.orderclient.model.OrderResponse;
import com.springbyexample.v2.servicediscovery.orderservice.OrderServiceApplication;

/**
 * Tests service registration, discovery, and communication between applications.
 *
 * @author Mujuzi Moses
 */
public class ServiceDiscoveryIntegrationTest {

    private static final String ORDER_SERVICE = "order-service";

    private static final String ORDER_ID = "ORDER-001";

    private static ConfigurableApplicationContext discoveryServer;

    private static ConfigurableApplicationContext orderService;

    private static ConfigurableApplicationContext orderClient;

    private static int orderClientPort;

    @BeforeAll
    static void startApplications() {
        discoveryServer = new SpringApplicationBuilder(
                DiscoveryServerApplication.class)
                .run(
                        "--server.port=0",
                        "--spring.application.name=discovery-server",
                        "--eureka.client.register-with-eureka=false",
                        "--eureka.client.fetch-registry=false"
                );

        int discoveryServerPort = portOf(discoveryServer);

        String discoveryUrl = "http://localhost:" + discoveryServerPort + "/eureka/";

        orderService = new SpringApplicationBuilder(
                OrderServiceApplication.class)
                .run(
                        "--server.port=0",
                        "--spring.application.name=order-service",
                        "--eureka.client.service-url.defaultZone=" + discoveryUrl,
                        "--eureka.client.register-with-eureka=true",
                        "--eureka.client.fetch-registry=true",
                        "--eureka.instance.prefer-ip-address=true",
                        "--eureka.client.jersey.enabled=false"
                );

        orderClient = new SpringApplicationBuilder(
                OrderClientApplication.class)
                .run(
                        "--server.port=0",
                        "--spring.application.name=order-client",
                        "--eureka.client.service-url.defaultZone=" + discoveryUrl,
                        "--eureka.client.register-with-eureka=true",
                        "--eureka.client.fetch-registry=true",
                        "--eureka.instance.prefer-ip-address=true",
                        "--eureka.client.jersey.enabled=false"
                );

        orderClientPort = portOf(orderClient);

        awaitOrderServiceDiscovery();
    }

    @AfterAll
    static void stopApplications() {
        closeContext(orderClient);
        closeContext(orderService);
        closeContext(discoveryServer);
    }

    @Test
    void orderClient_shouldDiscoverOrderServiceAndRetrieveOrder() {
        String url = "http://localhost:" + orderClientPort + "/api/orders/" + ORDER_ID;

        OrderResponse response = RestClient.create().get().uri(url).retrieve().body(OrderResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.service()).isEqualTo(ORDER_SERVICE);
    }

    private static void awaitOrderServiceDiscovery() {
        DiscoveryClient discoveryClient = orderClient.getBean(DiscoveryClient.class);
        long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();

        while (System.nanoTime() < deadline) {
            if (!discoveryClient.getInstances(ORDER_SERVICE).isEmpty()) {
                return;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for service discovery", exception);
            }
        }

        throw new IllegalStateException("Service '%s' was not discovered within the timeout".formatted(ORDER_SERVICE));
    }

    private static int portOf(ConfigurableApplicationContext context) {
        return Objects.requireNonNull(((WebServerApplicationContext) context).getWebServer()).getPort();
    }

    private static void closeContext(ConfigurableApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }
}
