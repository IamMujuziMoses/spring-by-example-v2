package com.springbyexample.v2.apigateway.agintegrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.springbyexample.v2.apigateway.aginventoryservice.InventoryResponse;
import com.springbyexample.v2.apigateway.aginventoryservice.InventoryServiceApplication;
import com.springbyexample.v2.apigateway.agorderservice.OrderResponse;
import com.springbyexample.v2.apigateway.agorderservice.OrderServiceApplication;

/**
 * Integration tests for API Gateway routing.
 *
 * @author Mujuzi Moses
 */
public class ApiGatewayIntegrationTest {

    private static ConfigurableApplicationContext orderServiceContext;

    private static ConfigurableApplicationContext inventoryServiceContext;

    private static Process gatewayProcess;

    private static RestClient gatewayClient;

    @BeforeAll
    static void startApplications() throws Exception {
        try {
            orderServiceContext = new SpringApplicationBuilder(OrderServiceApplication.class)
                    .web(WebApplicationType.SERVLET).run("--server.port=0");

            inventoryServiceContext = new SpringApplicationBuilder(InventoryServiceApplication.class)
                    .web(WebApplicationType.SERVLET).run("--server.port=0");

            int orderServicePort = getPort(orderServiceContext);
            int inventoryServicePort = getPort(inventoryServiceContext);
            int gatewayPort = findAvailablePort();

            startGateway(gatewayPort, orderServicePort, inventoryServicePort);

            gatewayClient = RestClient.builder().baseUrl("http://localhost:" + gatewayPort).build();

            waitForGateway(gatewayPort);

        } catch (Exception exception) {
            stopApplications();
            throw exception;
        }
    }

    @AfterAll
    static void stopApplications() {
        if (gatewayProcess != null) {
            gatewayProcess.destroy();

            try {
                if (!gatewayProcess.waitFor(5, TimeUnit.SECONDS)) {
                    gatewayProcess.destroyForcibly();
                }
            } catch (InterruptedException exception) {
                gatewayProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }

        closeContext(inventoryServiceContext);
        closeContext(orderServiceContext);
    }

    @Test
    void shouldRouteOrderRequestsToOrderService() {
        ResponseEntity<OrderResponse> response = gatewayClient.get().uri("/api/orders/ORD-1001").retrieve()
                .toEntity(OrderResponse.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().service()).isEqualTo("order-service");
        assertThat(response.getBody().orderId()).isEqualTo("ORD-1001");
        assertThat(response.getHeaders().getFirst("X-Gateway-Route")).isEqualTo("orders");
    }

    @Test
    void shouldRouteInventoryRequestsToInventoryService() {
        ResponseEntity<InventoryResponse> response = gatewayClient.get().uri("/api/inventory/SKU-1001").retrieve()
                .toEntity(InventoryResponse.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().service()).isEqualTo("inventory-service");
        assertThat(response.getBody().sku()).isEqualTo("SKU-1001");
        assertThat(response.getBody().availableQuantity()).isEqualTo(100);
        assertThat(response.getHeaders().getFirst("X-Gateway-Route")).isEqualTo("inventory");
    }

    @Test
    void shouldReturnNotFoundForUnmatchedRoutes() {
        ResponseEntity<String> response = gatewayClient.get().uri("/api/customers/CUST-1001")
                .exchange((request, clientResponse) ->
                        ResponseEntity.status(clientResponse.getStatusCode()).body(clientResponse.bodyTo(String.class)));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    private static void startGateway(int gatewayPort, int orderServicePort, int inventoryServicePort) throws IOException {
        Path gatewayDirectory = Path.of("../ag-gateway-server").toAbsolutePath().normalize();
        Path gatewayClasses = gatewayDirectory.resolve("target/classes");
        Path gatewayClasspathFile = gatewayDirectory.resolve("target/gateway-classpath.txt");

        if (!Files.isDirectory(gatewayClasses)) {
            throw new IllegalStateException("Gateway classes directory does not exist: " + gatewayClasses);
        }

        if (!Files.isRegularFile(gatewayClasspathFile)) {
            throw new IllegalStateException("Gateway classpath file does not exist: " + gatewayClasspathFile);
        }

        String gatewayClasspath = gatewayClasses + File.pathSeparator + Files.readString(gatewayClasspathFile).trim();
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java").toString();

        ProcessBuilder processBuilder = new ProcessBuilder(
                javaExecutable,
                "-cp",
                gatewayClasspath,
                "com.springbyexample.v2.apigateway.aggatewayserver.GatewayServerApplication",
                "--server.port=" + gatewayPort,
                "--services.order.url=http://localhost:" + orderServicePort,
                "--services.inventory.url=http://localhost:" + inventoryServicePort
        );

        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        gatewayProcess = processBuilder.start();
    }

    private static void waitForGateway(int gatewayPort) throws InterruptedException {
        RestClient client = RestClient.builder().baseUrl("http://localhost:" + gatewayPort).build();
        long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();

        while (System.nanoTime() < deadline) {
            if (!gatewayProcess.isAlive()) {
                throw new IllegalStateException("Gateway process terminated unexpectedly.");
            }

            try {
                client.get().uri("/api/orders/ORD-1001").retrieve().toBodilessEntity();
                return;
            } catch (Exception ignored) {
                Thread.sleep(250);
            }
        }

        throw new IllegalStateException("Gateway did not become available within 30 seconds.");
    }

    private static int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static int getPort(ConfigurableApplicationContext context) {
        WebServerApplicationContext webContext = (WebServerApplicationContext) context;
        return Objects.requireNonNull(webContext.getWebServer()).getPort();
    }

    private static void closeContext(ConfigurableApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }
}
