package com.springbyexample.v2.servicediscovery.orderclient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.springbyexample.v2.servicediscovery.orderclient.model.OrderResponse;

/**
 * Tests service discovery and HTTP communication with the Order Service.
 *
 * @author Mujuzi Moses
 */
public class OrderDiscoveryServiceTest {

    private static final String ORDER_SERVICE_URL = "http://localhost:8081";

    private static final String SERVICE_ID = "order-service";

    private static final String ORDER_ID = "ORDER-001";

    private DiscoveryClient discoveryClient;

    private MockRestServiceServer mockServer;

    private OrderDiscoveryService orderDiscoveryService;

    @BeforeEach
    void setUp() {
        discoveryClient = mock(DiscoveryClient.class);
        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        orderDiscoveryService = new OrderDiscoveryService(discoveryClient, restClientBuilder);
    }

    @Test
    void getOrder_shouldDiscoverOrderServiceAndRetrieveOrder() {
        ServiceInstance instance = new DefaultServiceInstance("order-service-1", "order-service",
                "localhost", 8081, false);

        when(discoveryClient.getInstances("order-service")).thenReturn(List.of(instance));

        mockServer.expect(requestTo(ORDER_SERVICE_URL + "/api/orders/order-123")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                                {
                                  "orderId": "order-123",
                                  "status": "ACCEPTED",
                                  "service": "order-service"
                                }
                                """,
                        MediaType.APPLICATION_JSON));

        OrderResponse response = orderDiscoveryService.getOrder("order-123");

        assertThat(response.orderId()).isEqualTo("order-123");
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.service()).isEqualTo("order-service");

        mockServer.verify();
    }

    @Test
    void getOrder_shouldThrowServiceUnavailableWhenNoInstancesAreAvailable() {
        when(discoveryClient.getInstances("order-service")).thenReturn(List.of());

        assertThatThrownBy(() -> orderDiscoveryService.getOrder("order-123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException statusException = (ResponseStatusException) exception;

                    assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    @Test
    void getOrder_shouldReturnOrderWhenServiceIsRegistered() {
        ServiceInstance instance = serviceInstance(ORDER_SERVICE_URL);

        when(discoveryClient.getInstances(SERVICE_ID)).thenReturn(List.of(instance));

        mockServer.expect(requestTo(ORDER_SERVICE_URL + "/api/orders/" + ORDER_ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "orderId": "ORDER-001",
                          "status": "ACCEPTED",
                          "service": "order-service"
                        }
                        """, MediaType.APPLICATION_JSON));

        OrderResponse response = orderDiscoveryService.getOrder(ORDER_ID);

        assertThat(response.orderId()).isEqualTo(ORDER_ID);
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.service()).isEqualTo(SERVICE_ID);

        mockServer.verify();
    }

    @Test
    void getOrder_shouldUseAnAvailableInstanceWhenMultipleInstancesAreAvailable() {
        ServiceInstance firstInstance = serviceInstance(ORDER_SERVICE_URL);
        ServiceInstance secondInstance = serviceInstance("http://localhost:8083");

        when(discoveryClient.getInstances(SERVICE_ID)).thenReturn(List.of(firstInstance, secondInstance));

        mockServer.expect(requestTo(ORDER_SERVICE_URL + "/api/orders/" + ORDER_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "orderId": "ORDER-001",
                          "status": "ACCEPTED",
                          "service": "order-service"
                        }
                        """, MediaType.APPLICATION_JSON));

        OrderResponse response = orderDiscoveryService.getOrder(ORDER_ID);

        assertThat(response.orderId()).isEqualTo(ORDER_ID);

        mockServer.verify();
    }

    @Test
    void getOrder_shouldPropagateHttpErrorWhenDownstreamServiceReturnsAnError() {
        when(discoveryClient.getInstances(SERVICE_ID)).thenReturn(List.of(serviceInstance(ORDER_SERVICE_URL)));

        mockServer.expect(requestTo(ORDER_SERVICE_URL + "/api/orders/" + ORDER_ID)).andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> orderDiscoveryService.getOrder(ORDER_ID)).isInstanceOf(RuntimeException.class);

        mockServer.verify();
    }

    private ServiceInstance serviceInstance(String uri) {
        return new DefaultServiceInstance("order-service-instance", SERVICE_ID, URI.create(uri).getHost(),
                URI.create(uri).getPort(), false);
    }
}
