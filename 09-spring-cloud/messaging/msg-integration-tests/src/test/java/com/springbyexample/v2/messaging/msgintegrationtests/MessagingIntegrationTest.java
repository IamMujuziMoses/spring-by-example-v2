package com.springbyexample.v2.messaging.msgintegrationtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import com.springbyexample.v2.messaging.msginventoryservice.InventoryServiceApplication;
import com.springbyexample.v2.messaging.msginventoryservice.model.InventoryItem;
import com.springbyexample.v2.messaging.msgorderservice.OrderServiceApplication;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderAcceptedResponse;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderRequest;

/**
 * Integration tests for asynchronous order messaging.
 *
 * @author Mujuzi Moses
 */
@Testcontainers
@EnabledIfSystemProperty(named = "skipMessagingIntegrationTest", matches = "false")
public class MessagingIntegrationTest {

    private static final String PRODUCT_ID = "SKU-1001";

    private static final int ORDER_QUANTITY = 5;

    private static final int INITIAL_QUANTITY = 25;

    @Container
    private static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management")
            ).waitingFor(Wait.forLogMessage(".*Server startup complete.*", 1));

    private static ConfigurableApplicationContext inventoryContext;

    private static ConfigurableApplicationContext orderContext;

    private static RestClient orderClient;

    private static RestClient inventoryClient;


    @BeforeAll
    static void startApplications() {
        String rabbitHost = RABBITMQ.getHost();
        String rabbitPort = String.valueOf(RABBITMQ.getAmqpPort());

        inventoryContext = new SpringApplicationBuilder(InventoryServiceApplication.class).web(WebApplicationType.SERVLET)
                .run(rabbitMqProducerProperties(rabbitHost, rabbitPort, "in"));

        orderContext = new SpringApplicationBuilder(OrderServiceApplication.class).web(WebApplicationType.SERVLET)
                .run(rabbitMqProducerProperties(rabbitHost, rabbitPort, "out"));

        int inventoryPort = getApplicationPort(inventoryContext);
        int orderPort = getApplicationPort(orderContext);

        inventoryClient = RestClient.builder().baseUrl("http://localhost:" + inventoryPort).build();
        orderClient = RestClient.builder().baseUrl("http://localhost:" + orderPort).build();
    }

    @AfterAll
    static void stopApplications() {
        if (orderContext != null) {
            orderContext.close();
        }

        if (inventoryContext != null) {
            inventoryContext.close();
        }
    }

    @Test
    void shouldPublishOrderCreatedEventAndUpdateInventory() {
        OrderRequest request = new OrderRequest("order-001", PRODUCT_ID, ORDER_QUANTITY);

        OrderAcceptedResponse response = orderClient.post()
                .uri("/api/orders")
                .body(request)
                .retrieve()
                .body(OrderAcceptedResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo("order-001");
        assertThat(response.status()).isEqualTo("ACCEPTED");

        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    InventoryItem inventoryItem = inventoryClient.get()
                            .uri("/api/inventory/{productId}", PRODUCT_ID)
                            .retrieve()
                            .body(InventoryItem.class);

                    assertThat(inventoryItem).isNotNull();
                    assertThat(inventoryItem.availableQuantity())
                            .isEqualTo(
                                    INITIAL_QUANTITY - ORDER_QUANTITY
                            );
                });
    }

    private static int getApplicationPort(ConfigurableApplicationContext context) {
        Integer port = context.getEnvironment().getProperty("local.server.port", Integer.class);

        if (port == null) {
            throw new IllegalStateException("Application server port was not assigned");
        }

        return port;
    }

    /**
     * Creates the command-line arguments required to configure a Spring Boot application with RabbitMQ and the Order
     * Created output binding.
     *
     * @param rabbitHost the RabbitMQ host
     * @param rabbitPort the RabbitMQ port
     * @return the application configuration arguments
     */
    private static String[] rabbitMqProducerProperties(String rabbitHost, String rabbitPort, String orderCreated) {
        return new String[]
                {
                        "--server.port=0",
                        "--spring.rabbitmq.host=" + rabbitHost,
                        "--spring.rabbitmq.port=" + rabbitPort,
                        "--spring.rabbitmq.username=guest",
                        "--spring.rabbitmq.password=guest",

                        "--spring.cloud.stream.bindings.orderCreated-" + orderCreated + "-0.destination=orders.created"
                };
    }
}
