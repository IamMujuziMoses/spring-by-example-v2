package com.springbyexample.v2.loadbalancing.lbintegrationtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import com.springbyexample.v2.loadbalancing.lborderclient.OrderClientApplication;
import com.springbyexample.v2.loadbalancing.lborderclient.client.OrderServiceClient;
import com.springbyexample.v2.loadbalancing.lborderclient.dto.OrderResponse;
import com.springbyexample.v2.loadbalancing.lborderservice.OrderServiceApplication;

/**
 * Integration tests demonstrating client-side load balancing between order service instances.
 *
 * @author Mujuzi Moses
 */
@Execution(ExecutionMode.SAME_THREAD)
public class LoadBalancingIntegrationTest {

    private static final String INSTANCE_ONE = "order-service-instance-1";

    private static final String INSTANCE_TWO = "order-service-instance-2";

    private static final String SERVICE_ID = "lb-order-service";

    private static final int REQUEST_COUNT = 10;

    private static ConfigurableApplicationContext orderServiceInstanceOne;

    private static ConfigurableApplicationContext orderServiceInstanceTwo;

    private static ConfigurableApplicationContext orderClientApplication;

    @BeforeAll
    static void startApplications() {
        orderServiceInstanceOne = startOrderService(INSTANCE_ONE);
        orderServiceInstanceTwo = startOrderService(INSTANCE_TWO);

        String instanceOneUri = serviceUri(orderServiceInstanceOne);
        String instanceTwoUri = serviceUri(orderServiceInstanceTwo);

        orderClientApplication = new SpringApplicationBuilder(OrderClientApplication.class)
                .run(
                        "--server.port=0",
                        "--spring.cloud.discovery.client.simple.instances." + SERVICE_ID + "[0].uri=" + instanceOneUri,
                        "--spring.cloud.discovery.client.simple.instances." + SERVICE_ID + "[1].uri=" + instanceTwoUri,
                        "--spring.main.banner-mode=off"
                );
    }

    @AfterAll
    static void stopApplications() {
        closeContext(orderClientApplication);
        closeContext(orderServiceInstanceTwo);
        closeContext(orderServiceInstanceOne);
    }

    @Test
    void shouldReturnOrderFromAvailableServiceInstance() {
        OrderResponse response = requestOrder("1001");

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isEqualTo("1001");
        assertThat(response.instanceId()).isIn(INSTANCE_ONE, INSTANCE_TWO);
    }

    @Test
    void shouldDistributeRequestsAcrossOrderServiceInstances() {
        List<String> instanceIds = List.of(requestOrder("1001").instanceId(), requestOrder("1002")
                .instanceId());

        assertThat(instanceIds).contains(INSTANCE_ONE, INSTANCE_TWO).doesNotHaveDuplicates();
    }

    @Test
    void shouldDistributeRequestsEvenlyUsingRoundRobinStrategy() {
        List<OrderResponse> responses = IntStream.range(0, REQUEST_COUNT)
                .mapToObj(index -> requestOrder("1001")).toList();

        Map<String, Long> requestCounts = responses.stream().collect(Collectors.groupingBy(OrderResponse::instanceId,
                Collectors.counting()));

        assertThat(requestCounts).containsOnlyKeys(INSTANCE_ONE, INSTANCE_TWO);
        assertThat(requestCounts.get(INSTANCE_ONE)).isEqualTo(5L);
        assertThat(requestCounts.get(INSTANCE_TWO)).isEqualTo(5L);
    }

    @Test
    void shouldUseConfiguredServiceInstances() {
        DiscoveryClient discoveryClient = orderClientApplication.getBean(DiscoveryClient.class);

        List<URI> discoveredInstances = discoveryClient.getInstances(SERVICE_ID).stream().map(ServiceInstance::getUri)
                .toList();

        assertThat(discoveredInstances).containsExactlyInAnyOrder(URI.create(serviceUri(orderServiceInstanceOne)),
                URI.create(serviceUri(orderServiceInstanceTwo)));
    }

    @Test
    void shouldHandleUnavailableServiceInstances() {
        try (ConfigurableApplicationContext emptyClientApplication = startClientWithoutServiceInstances()) {

            OrderServiceClient orderServiceClient = emptyClientApplication.getBean(OrderServiceClient.class);

            assertThatThrownBy(() -> orderServiceClient.getOrder("1001")).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No instances available for " + SERVICE_ID);
        }
    }

    private static ConfigurableApplicationContext startOrderService(String instanceId) {
        return new SpringApplicationBuilder(OrderServiceApplication.class)
                .run(
                        "--server.port=0",
                        "--app.instance-id=" + instanceId,
                        "--spring.main.banner-mode=off"
                );
    }

    private static ConfigurableApplicationContext startClientWithoutServiceInstances() {
        return new SpringApplicationBuilder(OrderClientApplication.class)
                .run(
                        "--spring.config.name=application-without-service-instances",
                        "--server.port=0",
                        "--spring.main.banner-mode=off"
                );
    }

    private static OrderResponse requestOrder(String orderId) {
        OrderServiceClient orderServiceClient = orderClientApplication.getBean(OrderServiceClient.class);

        return orderServiceClient.getOrder(orderId);
    }

    private static String serviceUri(ConfigurableApplicationContext context) {
        Integer port = context.getEnvironment().getRequiredProperty("local.server.port", Integer.class);

        return "http://localhost:" + port;
    }

    private static void closeContext(ConfigurableApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }
}
