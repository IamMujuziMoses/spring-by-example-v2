package com.springbyexample.v2.backpressure;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class BackPressureApplicationTest {

    @Test
    void request_shouldEmitOnlyRequestedNumberOfValues() {
        // Initial demand is zero, so no values should be emitted
        // until the subscriber explicitly requests them.
        Flux<Integer> numbers = Flux.range(1, 5);

        StepVerifier.create(numbers, 0)
                .thenRequest(2)
                .expectNext(1, 2)
                .thenCancel()
                .verify();
    }

    @Test
    void request_shouldAllowDemandToBeAddedIncrementally() {
        // Each request adds to the subscriber's outstanding demand.
        // The publisher can emit values as that demand is made available.
        Flux<Integer> numbers = Flux.range(1, 5);

        StepVerifier.create(numbers, 0)
                .thenRequest(2)
                .expectNext(1, 2)
                .thenRequest(1)
                .expectNext(3)
                .thenRequest(2)
                .expectNext(4, 5)
                .verifyComplete();
    }

    @Test
    void request_shouldCompleteWhenAllValuesAreConsumed() {
        // Requesting enough elements allows the finite publisher
        // to emit its complete sequence and signal completion.
        Flux<Integer> numbers = Flux.range(1, 3);

        StepVerifier.create(numbers, 0)
                .thenRequest(3)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    void limitRate_shouldManageUpstreamRequestBatches() {
        // limitRate() reshapes requests made upstream into smaller batches.
        // It is useful for demonstrating request coordination between
        // a downstream subscriber and an upstream publisher.
        Flux<Integer> numbers = Flux.range(1, 10).limitRate(3);

        StepVerifier.create(numbers, 0)
                .thenRequest(2)
                .expectNext(1, 2)
                .thenRequest(3)
                .expectNext(3, 4, 5)
                .thenRequest(5)
                .expectNext(6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    void backpressureBuffer_shouldHandleValuesBeyondCurrentDemand() {
        // onBackpressureBuffer() requests upstream data and buffers
        // values that cannot immediately be delivered downstream.
        // The buffer allows the subscriber to consume those values later.
        Flux<Integer> numbers = Flux.range(1, 5).onBackpressureBuffer();

        StepVerifier.create(numbers, 0)
                .thenRequest(2)
                .expectNext(1, 2)
                .thenRequest(3)
                .expectNext(3, 4, 5)
                .verifyComplete();
    }
}