package com.springbyexample.v2.distributedtracing.dtorderservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.springbyexample.v2.distributedtracing.dtorderservice.model.InventoryResponse;
import com.springbyexample.v2.distributedtracing.dtorderservice.model.OrderRequest;
import com.springbyexample.v2.distributedtracing.dtorderservice.model.OrderResponse;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

/**
 * Handles order processing and inventory verification.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderService {

    private final RestClient inventoryRestClient;

    private final ObservationRegistry observationRegistry;

    private final Tracer tracer;

    public OrderService(RestClient inventoryRestClient, ObservationRegistry observationRegistry, Tracer tracer) {
        this.inventoryRestClient = inventoryRestClient;
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
    }

    public OrderResponse process(OrderRequest request) {
        return Observation.createNotStarted("order.process", observationRegistry)
                .observe(() -> {
                    Span currentSpan = tracer.currentSpan();
                    String traceId = currentSpan != null ? currentSpan.context().traceId() : null;

                    InventoryResponse inventory = inventoryRestClient
                            .get()
                            .uri("/api/inventory/{productId}", request.productId())
                            .retrieve()
                            .body(InventoryResponse.class);

                    if (inventory == null) {
                        throw new IllegalStateException("Inventory Service returned an empty response");
                    }

                    boolean available = inventory.availableQuantity() >= request.quantity();

                    return new OrderResponse(request.orderId(), request.productId(), request.quantity(),
                            inventory.availableQuantity(), available ? "ACCEPTED" : "REJECTED", traceId,
                            inventory.traceId());
                });
    }
}

