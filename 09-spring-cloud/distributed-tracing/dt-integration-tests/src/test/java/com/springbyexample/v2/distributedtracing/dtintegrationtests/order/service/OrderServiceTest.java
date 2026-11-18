package com.springbyexample.v2.distributedtracing.dtintegrationtests.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.springbyexample.v2.distributedtracing.dtorderservice.model.InventoryResponse;
import com.springbyexample.v2.distributedtracing.dtorderservice.model.OrderRequest;
import com.springbyexample.v2.distributedtracing.dtorderservice.model.OrderResponse;
import com.springbyexample.v2.distributedtracing.dtorderservice.service.OrderService;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

/**
 * Unit tests for OrderService.
 *
 * @author Mujuzi Moses
 */
public class OrderServiceTest {

    private static final String TRACE_ID = "0123456789abcdef0123456789abcdef";

    private static final String PRODUCT_ID = "SKU-1001";

    private RestClient inventoryRestClient;

    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    private RestClient.ResponseSpec responseSpec;

    private Tracer tracer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        inventoryRestClient = mock(RestClient.class);
        requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        ObservationRegistry observationRegistry = ObservationRegistry.create();

        tracer = mock(Tracer.class);
        orderService = new OrderService(inventoryRestClient, observationRegistry, tracer);
    }

    @Test
    void shouldAcceptOrderWhenInventoryIsSufficient() {
        mockInventoryResponse(25);

        OrderResponse response = orderService.process(new OrderRequest("ORD-1001", PRODUCT_ID, 2));

        assertThat(response.orderId()).isEqualTo("ORD-1001");
        assertThat(response.productId()).isEqualTo(PRODUCT_ID);
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.availableQuantity()).isEqualTo(25);
        assertThat(response.status()).isEqualTo("ACCEPTED");
        assertThat(response.inventoryTraceId()).isNull();
    }

    @Test
    void shouldRejectOrderWhenInventoryIsInsufficient() {
        mockInventoryResponse(1);

        OrderResponse response = orderService.process(new OrderRequest("ORD-1002", PRODUCT_ID, 2));

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.availableQuantity()).isEqualTo(1);
    }

    @Test
    void shouldRejectOrderWhenProductIsUnavailable() {
        mockInventoryResponse(0);

        OrderResponse response = orderService.process(new OrderRequest("ORD-1003", "SKU-9999", 1));

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.availableQuantity()).isZero();
    }

    @Test
    void shouldCaptureCurrentTraceId() {
        mockInventoryResponse(25);

        Span span = mock(Span.class);
        TraceContext traceContext = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(TRACE_ID);

        OrderResponse response = orderService.process(new OrderRequest("ORD-1004", PRODUCT_ID, 2));

        assertThat(response.traceId()).isEqualTo(TRACE_ID);
    }

    @Test
    void shouldPropagateInventoryServiceFailure() {
        mockInventoryRequest();

        when(responseSpec.body(any(Class.class))).thenThrow(new IllegalStateException("Inventory unavailable"));

        assertThatThrownBy(() -> orderService.process(new OrderRequest("ORD-1005", PRODUCT_ID, 2)
        )).isInstanceOf(IllegalStateException.class).hasMessage("Inventory unavailable");
    }

    @Test
    void shouldThrowExceptionWhenInventoryServiceReturnsEmptyResponse() {
        mockInventoryRequest();

        when(responseSpec.body(any(Class.class))).thenReturn(null);

        assertThatThrownBy(() -> orderService.process(new OrderRequest("ORD-1006", PRODUCT_ID, 2)
        )).isInstanceOf(IllegalStateException.class).hasMessage("Inventory Service returned an empty response");
    }

    private void mockInventoryResponse(int availableQuantity) {
        InventoryResponse inventoryResponse = new InventoryResponse(PRODUCT_ID, availableQuantity, null);

        mockInventoryRequest();

        when(responseSpec.body(any(Class.class))).thenReturn(inventoryResponse);
    }

    private void mockInventoryRequest() {
        doAnswer(invocation -> requestHeadersUriSpec).when(inventoryRestClient).get();

        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(String.class), any(Object[].class));

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }
}
