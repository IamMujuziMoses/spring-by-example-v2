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
 * Verifies failure scenarios in the Order Management application.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OrderManagementFailureScenarioTest {

    @Value("${verification.base-url}")
    private String baseUrl;

    @Value("${verification.customer.username}")
    private String customerUsername;

    @Value("${verification.customer.password}")
    private String customerPassword;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void placeOrder_shouldReturnBadRequestForInvalidQuantity() throws Exception {
        long productId = findProductId("Mouse");

        HttpResponse<String> response = post(
                "/api/orders",
                """
                        {
                            "productId": %d,
                            "quantity": 0
                        }
                        """.formatted(productId),
                customerUsername, customerPassword);

        assertEquals(400, response.statusCode());
    }

    @Test
    void placeOrder_shouldReturnConflictWhenStockIsInsufficient() throws Exception {
        long productId = findProductId("Mouse");

        HttpResponse<String> response = post(
                "/api/orders",
                """
                        {
                            "productId": %d,
                            "quantity": 100000
                        }
                        """.formatted(productId),
                customerUsername, customerPassword);

        assertEquals(409, response.statusCode());
    }

    @Test
    void placeOrder_shouldReturnNotFoundForUnknownProduct() throws Exception {
        long unknownProductId = Long.MAX_VALUE;

        HttpResponse<String> response = post(
                "/api/orders",
                """
                        {
                            "productId": %d,
                            "quantity": 1
                        }
                        """.formatted(unknownProductId),
                customerUsername, customerPassword);

        assertEquals(404, response.statusCode());
    }

    private long findProductId(String name) throws Exception {
        HttpResponse<String> response = get("/api/products");

        assertEquals(200, response.statusCode());

        JsonNode products = objectMapper.readTree(response.body());

        assertTrue(products.isArray());
        assertFalse(products.isEmpty());

        for (JsonNode product : products) {
            if (name.equals(product.path("name").asText())) {
                return product.path("id").asLong();
            }
        }

        throw new AssertionError("Expected product was not found: " + name);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + path)).GET().build();

        return send(request);
    }

    private HttpResponse<String> post(String path, String body, String username, String password) throws IOException,
            InterruptedException {

        HttpRequest request = authenticatedRequest(path, username, password)
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return send(request);
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
