package com.springbyexample.v2.distributedtracing.dtintegrationtests.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.distributedtracing.dtinventoryservice.model.InventoryResponse;
import com.springbyexample.v2.distributedtracing.dtinventoryservice.service.InventoryService;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

/**
 * Unit tests for InventoryService.
 *
 * @author Mujuzi Moses
 */
public class InventoryServiceTest {

    private static final String TRACE_ID = "0123456789abcdef0123456789abcdef";

    private InventoryService inventoryService;

    private Tracer tracer;

    @BeforeEach
    void setUp() {
        ObservationRegistry observationRegistry = ObservationRegistry.create();

        tracer = mock(Tracer.class);
        inventoryService = new InventoryService(observationRegistry, tracer);
    }

    @Test
    void shouldReturnAvailableQuantityForKnownProduct() {
        InventoryResponse response = inventoryService.lookup("SKU-1001");

        assertThat(response.productId()).isEqualTo("SKU-1001");
        assertThat(response.availableQuantity()).isEqualTo(25);
    }

    @Test
    void shouldReturnZeroQuantityForUnknownProduct() {
        InventoryResponse response = inventoryService.lookup("SKU-9999");

        assertThat(response.productId()).isEqualTo("SKU-9999");
        assertThat(response.availableQuantity()).isZero();
    }

    @Test
    void shouldReturnCurrentTraceIdWhenTraceIsAvailable() {
        Span span = mock(Span.class);
        TraceContext traceContext = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(TRACE_ID);

        InventoryResponse response = inventoryService.lookup("SKU-1001");

        assertThat(response.traceId()).isEqualTo(TRACE_ID);
    }

    @Test
    void shouldReturnNullTraceIdWhenNoTraceIsAvailable() {
        when(tracer.currentSpan()).thenReturn(null);

        InventoryResponse response = inventoryService.lookup("SKU-1001");

        assertThat(response.traceId()).isNull();
    }
}
