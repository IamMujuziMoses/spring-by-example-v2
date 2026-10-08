package com.springbyexample.v2.reactivepipelines;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class ReactivePipelinesApplicationTest {

    @Test
    void pipeline_shouldComposeMultipleOperators() {
        // A reactive pipeline is created by chaining operators together. Each operator receives the result of the
        // previous stage and returns another publisher for the next stage.
        Flux<String> result = Flux.just("spring", "reactor", "webflux", "data")
                .filter(value -> value.startsWith("r") || value.startsWith("w"))
                .map(String::toUpperCase)
                .map(value -> "Topic: " + value);

        StepVerifier.create(result).expectNext("Topic: REACTOR", "Topic: WEBFLUX").verifyComplete();
    }

    @Test
    void pipeline_shouldPreserveTheOrderOfValues() {
        // Operators normally process values in the order in which they are received by the pipeline.
        Flux<String> result = Flux.just("first", "second", "third").map(String::toUpperCase);

        StepVerifier.create(result).expectNext("FIRST", "SECOND", "THIRD").verifyComplete();
    }

    @Test
    void flatMap_shouldTransformEachValueIntoAnotherPublisher() {
        // flatMap() is useful when transforming each value requires another reactive publisher.
        //
        // Unlike map(), which transforms a value into another value, flatMap() transforms a value into a Publisher and
        // then merges the resulting publishers.
        Flux<String> result = Flux.just("John", "Jane").flatMap(name -> Mono.just("Hello " + name));

        StepVerifier.create(result).expectNextCount(2).verifyComplete();
    }

    @Test
    void concatMap_shouldPreserveSourceOrder() {
        // concatMap() also transforms each value into another publisher, but subscribes to those publishers sequentially.
        //
        // This makes concatMap() useful when the order of the source sequence must be preserved.
        Flux<String> result = Flux.just("John", "Jane", "Peter")
                .concatMap(name -> Mono.just("Hello " + name));

        StepVerifier.create(result).expectNext("Hello John", "Hello Jane", "Hello Peter").verifyComplete();
    }

    @Test
    void flatMapMany_shouldExpandMonoIntoFlux() {
        // A Mono can be expanded into multiple values with flatMapMany().
        //
        // This is useful when a single asynchronous result determines a subsequent multi-value reactive sequence.
        Mono<List<String>> names = Mono.just(List.of("John", "Jane", "Peter"));

        Flux<String> result = names.flatMapMany(Flux::fromIterable);

        StepVerifier.create(result).expectNext("John", "Jane", "Peter").verifyComplete();
    }

    @Test
    void then_shouldIgnoreValuesAndContinueWithAnotherPublisher() {
        // then() waits for the source publisher to complete, ignores its emitted values, and then subscribes to
        // the publisher supplied to then().
        Flux<String> result = Flux.just("John", "Jane").thenMany(Flux.just("Complete"));

        StepVerifier.create(result).expectNext("Complete").verifyComplete();
    }

    @Test
    void switchIfEmpty_shouldSwitchToFallbackPublisher() {
        // switchIfEmpty() provides another publisher when the original publisher completes without emitting a value.
        Flux<String> result = Flux.<String>empty().switchIfEmpty(Flux.just("No results"));

        StepVerifier.create(result).expectNext("No results").verifyComplete();
    }

    @Test
    void pipeline_shouldBeLazyUntilSubscription() {
        // Building a pipeline does not execute it immediately. Execution begins when a subscriber subscribes.
        boolean[] executed = { false };

        Flux<String> result = Flux.defer(() -> {
            executed[0] = true;

            return Flux.just("Spring", "Reactor");
        }).map(String::toUpperCase);

        assertThat(executed[0]).isFalse();
        StepVerifier.create(result).expectNext("SPRING", "REACTOR").verifyComplete();
        assertThat(executed[0]).isTrue();
    }

    @Test
    void pipeline_shouldAllowDifferentPublisherTypesToBeComposed() {
        // Mono and Flux can participate in the same reactive pipeline. Here, a Mono provides one value which is then
        // expanded into a Flux containing multiple values.
        Mono<String> category = Mono.just("Spring");

        Flux<String> result = category.flatMapMany(value ->
                Flux.just(
                        value + " Core",
                        value + " Boot",
                        value + " Data"
                ));

        StepVerifier.create(result).expectNext("Spring Core", "Spring Boot", "Spring Data").verifyComplete();
    }
}