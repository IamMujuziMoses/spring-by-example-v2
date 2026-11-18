package com.springbyexample.v2.distributedtracing.dtintegrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import com.springbyexample.v2.distributedtracing.dtinventoryservice.InventoryServiceApplication;
import com.springbyexample.v2.distributedtracing.dtorderservice.OrderServiceApplication;

/**
 * Integration tests verifying HTTP behaviour and distributed trace propagation.
 *
 * @author Mujuzi Moses
 */
class DistributedTracingIntegrationTest {

    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("\"traceId\":\"([0-9a-f]{32})\"");

    private static final Pattern INVENTORY_TRACE_ID_PATTERN = Pattern.compile("\"inventoryTraceId\":\"([0-9a-f]{32})\"");

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private ConfigurableApplicationContext inventoryContext;

    private ConfigurableApplicationContext orderContext;

    private int orderPort;

    @BeforeEach
    void startServices() {
        inventoryContext = startInventoryService();

        int inventoryPort = getPort(inventoryContext);

        orderContext = startOrderService(inventoryPort);
        orderPort = getPort(orderContext);
    }

    @AfterEach
    void stopServices() {
        if (orderContext != null) {
            orderContext.close();
        }

        if (inventoryContext != null) {
            inventoryContext.close();
        }
    }

    @Test
    void shouldAcceptOrderWhenInventoryIsSufficient() throws Exception {
        HttpResponse<String> response = submitOrder("ORD-1001", "SKU-1001", 2);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"status\":\"ACCEPTED\"");
        assertThat(response.body()).contains("\"availableQuantity\":25");
    }

    @Test
    void shouldRejectOrderWhenInventoryIsInsufficient() throws Exception {
        HttpResponse<String> response = submitOrder("ORD-1002", "SKU-2002", 20);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"status\":\"REJECTED\"");
        assertThat(response.body()).contains("\"availableQuantity\":10");
    }

    @Test
    void shouldRejectOrderWhenProductDoesNotExist() throws Exception {
        HttpResponse<String> response = submitOrder("ORD-1003", "SKU-9999", 1);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("\"status\":\"REJECTED\"");
        assertThat(response.body()).contains("\"availableQuantity\":0");
    }

    @Test
    void shouldReturnBadRequestWhenOrderRequestIsInvalid() throws Exception {
        HttpResponse<String> response = submitOrder("", "SKU-1001", 0);

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldPropagateTraceContextAcrossServices() throws Exception {
        HttpResponse<String> response = submitOrder("ORD-1004", "SKU-1001", 2);

        assertThat(response.statusCode()).isEqualTo(201);

        String traceId = extractTraceId(TRACE_ID_PATTERN, response.body());
        String inventoryTraceId = extractTraceId(
                INVENTORY_TRACE_ID_PATTERN,
                response.body()
        );

        assertThat(traceId).isEqualTo(inventoryTraceId);
    }

    private HttpResponse<String> submitOrder(String orderId, String productId, int quantity) throws Exception {
        String requestBody = """
                {
                  "orderId": "%s",
                  "productId": "%s",
                  "quantity": %d
                }
                """.formatted(orderId, productId, quantity);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:" + orderPort + "/api/orders"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private ConfigurableApplicationContext startInventoryService() {
        return new SpringApplicationBuilder(InventoryServiceApplication.class)
                .run(
                        "--server.port=0",
//                        "--management.opentelemetry.tracing.export.otlp.enabled=false",
                        "--management.tracing.export.otlp.enabled=false"
                );
    }

    private ConfigurableApplicationContext startOrderService(int inventoryPort) {
        return new SpringApplicationBuilder(OrderServiceApplication.class)
                .run(
                        "--server.port=0",
                        "--inventory.service.url=http://localhost:" + inventoryPort,
//                        "--management.opentelemetry.tracing.export.otlp.enabled=false",
                        "--management.tracing.export.otlp.enabled=false"
                );
    }

    private int getPort(ConfigurableApplicationContext context) {
        return Objects.requireNonNull(((WebServerApplicationContext) context).getWebServer()).getPort();
    }

    private String extractTraceId(Pattern pattern, String responseBody) {
        Matcher matcher = pattern.matcher(responseBody);

        assertThat(matcher.find()).as("Expected trace ID in response: %s", responseBody).isTrue();

        return matcher.group(1);
    }
}
