package com.springbyexample.v2.distributedtracing.dtinventoryservice.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.distributedtracing.dtinventoryservice.model.InventoryResponse;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

/**
 * Handles inventory lookup operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class InventoryService {

    private static final Map<String, Integer> INVENTORY = Map.of("SKU-1001", 25, "SKU-2002", 10);

    private final ObservationRegistry observationRegistry;

    private final Tracer tracer;

    public InventoryService(ObservationRegistry observationRegistry, Tracer tracer) {
        this.observationRegistry = observationRegistry;
        this.tracer = tracer;
    }

    public InventoryResponse lookup(String productId) {
        return Observation.createNotStarted("inventory.lookup", observationRegistry)
                .observe(() -> {
                    Span currentSpan = tracer.currentSpan();

                    return new InventoryResponse(productId, INVENTORY.getOrDefault(productId, 0),
                            currentSpan != null ? currentSpan.context().traceId() : null);
                });
    }
}

