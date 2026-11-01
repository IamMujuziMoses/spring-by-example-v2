package com.springbyexample.v2.endtoendapplicationverification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifies the Order Management Application through its public HTTP API.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OrderManagementEndToEndTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${verification.base-url}")
    private String baseUrl;

    @Value("${verification.customer.username}")
    private String customerUsername;

    @Value("${verification.customer.password}")
    private String customerPassword;

    @Value("${verification.admin.username}")
    private String adminUsername;

    @Value("${verification.admin.password}")
    private String adminPassword;

    @Value("${verification.customer2.username}")
    private String secondCustomerUsername;

    @Value("${verification.customer2.password}")
    private String secondCustomerPassword;

    @Test
    void shouldVerifyCompleteOrderManagementWorkflow() throws Exception {
        HttpResponse<String> productsResponse = get("/api/products");

        assertEquals(200, productsResponse.statusCode());

        JsonNode products = objectMapper.readTree(productsResponse.body());

        assertTrue(products.isArray());
        assertFalse(products.isEmpty());

        JsonNode mouse = findProduct(products, "Mouse");
        long productId = mouse.path("id").asLong();

        HttpResponse<String> unauthenticatedOrdersResponse = get("/api/orders");

        assertEquals(401, unauthenticatedOrdersResponse.statusCode());

        HttpResponse<String> customerInventoryResponse = get("/api/inventory/" + productId, customerUsername,
                customerPassword);

        assertEquals(403, customerInventoryResponse.statusCode());

        HttpResponse<String> adminInventoryResponse = get("/api/inventory/" + productId, adminUsername, adminPassword);

        assertEquals(200, adminInventoryResponse.statusCode());

        int availableQuantity = objectMapper.readTree(adminInventoryResponse.body()).path("availableQuantity")
                .asInt();

        assertTrue(availableQuantity > 0);

        HttpResponse<String> customerOrdersResponse = get("/api/orders", customerUsername, customerPassword);

        assertEquals(200, customerOrdersResponse.statusCode());

        HttpResponse<String> orderResponse = post(
                "/api/orders",
                """
                        {
                            "productId": %d,
                            "quantity": 1
                        }
                        """.formatted(productId),
                customerUsername, customerPassword);

        assertEquals(201, orderResponse.statusCode());

        JsonNode createdOrder = objectMapper.readTree(orderResponse.body());

        long orderId = createdOrder.path("id").asLong();

        assertTrue(orderId > 0, "The created order should have an ID.");

        HttpResponse<String> updatedInventoryResponse = get("/api/inventory/" + productId, adminUsername,
                adminPassword);

        assertEquals(200, updatedInventoryResponse.statusCode());

        int updatedQuantity = objectMapper.readTree(updatedInventoryResponse.body()).path("availableQuantity")
                .asInt();

        assertEquals(availableQuantity - 1, updatedQuantity);

        // Verify that the created order appears in customer order history.
        HttpResponse<String> updatedOrdersResponse = get("/api/orders", customerUsername, customerPassword);

        assertEquals(200, updatedOrdersResponse.statusCode());

        JsonNode orders = objectMapper.readTree(updatedOrdersResponse.body());

        assertTrue(orders.isArray());
        assertTrue(containsOrder(orders, orderId), "The created order should appear in customer order history.");

        // Verify that the customer can retrieve the individual order.
        HttpResponse<String> retrievedOrderResponse = get("/api/orders/" + orderId, customerUsername, customerPassword);

        assertEquals(200, retrievedOrderResponse.statusCode());

        JsonNode retrievedOrder = objectMapper.readTree(retrievedOrderResponse.body());

        assertEquals(orderId, retrievedOrder.path("id").asLong());

        // Verify that another customer cannot access this order.
        HttpResponse<String> unauthorizedOrderResponse = get("/api/orders/" + orderId, secondCustomerUsername,
                secondCustomerPassword);

        assertEquals(404, unauthorizedOrderResponse.statusCode());

        // Verify that audit entries are restricted to administrators.
        HttpResponse<String> unauthenticatedAuditResponse = get("/api/audit");

        assertEquals(401, unauthenticatedAuditResponse.statusCode());

        HttpResponse<String> customerAuditResponse = get(
                "/api/audit",
                customerUsername,
                customerPassword);

        assertEquals(403, customerAuditResponse.statusCode());

        // Verify that the administrator can retrieve the persisted audit entry.
        HttpResponse<String> adminAuditResponse = get("/api/audit", adminUsername, adminPassword);

        assertEquals(200, adminAuditResponse.statusCode());

        JsonNode auditEntries = objectMapper.readTree(adminAuditResponse.body());

        assertTrue(auditEntries.isArray());

        boolean auditEntryFound = false;

        for (JsonNode entry : auditEntries) {
            if (entry.path("aggregateId").asLong() == orderId
                    && "ORDER_PLACED".equals(entry.path("eventType").asText())) {
                auditEntryFound = true;
                break;
            }
        }

        assertTrue(auditEntryFound, "An ORDER_PLACED audit entry should exist for the created order.");

    }

    @Test
    void placeOrder_shouldReturnNotFoundForUnknownProduct() throws Exception {
        long unknownProductId = Long.MAX_VALUE;

        String requestBody = """
            {
                "productId": %d,
                "quantity": 1
            }
            """.formatted(unknownProductId);

        HttpResponse<String> response = post("/api/orders", requestBody, customerUsername, customerPassword);

        assertEquals(404, response.statusCode());
    }

    private JsonNode findProduct(JsonNode products, String name) {

        for (JsonNode product : products) {
            if (name.equals(product.path("name").asText())) {
                return product;
            }
        }

        throw new AssertionError("Expected product was not found: " + name);
    }

    private boolean containsOrder(JsonNode orders, long orderId) {
        for (JsonNode order : orders) {
            if (order.path("id").asLong() == orderId) {
                return true;
            }
        }

        return false;
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return send(HttpRequest.newBuilder().uri(URI.create(baseUrl + path)).GET().build());
    }

    private HttpResponse<String> get(String path, String username, String password) throws IOException,
            InterruptedException {

        return send(authenticatedRequest(path, username, password).GET().build());
    }

    private HttpResponse<String> post(String path, String body, String username, String password) throws IOException,
            InterruptedException {

        return send(authenticatedRequest(path, username, password).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build());
    }

    private HttpRequest.Builder authenticatedRequest(String path, String username, String password) {

        String credentials = username + ":" + password;

        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return HttpRequest.newBuilder().uri(URI.create(baseUrl + path)).header("Authorization", "Basic "
                + encodedCredentials);
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
