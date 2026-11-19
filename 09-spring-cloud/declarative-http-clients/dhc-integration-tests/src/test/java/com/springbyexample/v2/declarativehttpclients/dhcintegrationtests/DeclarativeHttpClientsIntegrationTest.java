package com.springbyexample.v2.declarativehttpclients.dhcintegrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.springbyexample.v2.declarativehttpclients.dhcinventoryservice.InventoryServiceApplication;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.OrderServiceApplication;

/**
 * Integration tests for declarative HTTP clients.
 *
 * @author Mujuzi Moses
 */
public class DeclarativeHttpClientsIntegrationTest {

    private static ConfigurableApplicationContext inventoryContext;

    private static ConfigurableApplicationContext orderContext;

    private static HttpClient httpClient;

    private static int orderServicePort;

    @BeforeAll
    static void setUp() {
        httpClient = HttpClient.newHttpClient();
        inventoryContext = new SpringApplicationBuilder(InventoryServiceApplication.class)
                .run("--server.port=0", "--spring.main.banner-mode=off");

        int inventoryServicePort = Objects.requireNonNull(inventoryContext.getEnvironment()
                        .getProperty("local.server.port", Integer.class),
                "Inventory Service port was not assigned");

        orderContext = new SpringApplicationBuilder(OrderServiceApplication.class)
                .run(
                        "--server.port=0",
                        "--spring.main.banner-mode=off",
                        "--inventory.service.url=http://localhost:" + inventoryServicePort
                );

        orderServicePort = Objects.requireNonNull(orderContext.getEnvironment()
                        .getProperty("local.server.port", Integer.class),
                "Order Service port was not assigned");
    }

    @AfterAll
    static void tearDown() {
        if (orderContext != null) {
            orderContext.close();
        }

        if (inventoryContext != null) {
            inventoryContext.close();
        }
    }

    @Test
    void shouldAcceptOrderWhenInventoryIsSufficient() throws Exception {
        HttpResponse<String> response = createOrder("ORD-1001", "SKU-1001", 2);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains(
                "\"orderId\":\"ORD-1001\"",
                "\"productId\":\"SKU-1001\"",
                "\"quantity\":2",
                "\"availableQuantity\":25",
                "\"status\":\"ACCEPTED\""
        );
    }

    @Test
    void shouldRejectOrderWhenInventoryIsInsufficient() throws Exception {
        HttpResponse<String> response = createOrder("ORD-1002", "SKU-3003", 10);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"availableQuantity\":5", "\"status\":\"REJECTED\"");
    }

    @Test
    void shouldRejectOrderWhenProductDoesNotExist() throws Exception {
        HttpResponse<String> response = createOrder("ORD-1003", "SKU-9999", 1);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"availableQuantity\":0", "\"status\":\"REJECTED\"");
    }

    @Test
    void shouldReturnBadRequestWhenOrderRequestIsInvalid() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + orderServicePort + "/api/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                            "orderId": "",
                            "productId": "SKU-1001",
                            "quantity": 0
                        }
                        """)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
    }

    private static HttpResponse<String> createOrder(String orderId, String productId, int quantity) throws Exception {
        String requestBody = """
                {
                    "orderId": "%s",
                    "productId": "%s",
                    "quantity": %d
                }
                """.formatted(orderId, productId, quantity);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + orderServicePort + "/api/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
