package com.springbyexample.v2.mono;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class MonoApplicationTest {

    @Test
    void just_shouldEmitSingleValue() {
        Mono<String> name = Mono.just("John");

        StepVerifier.create(name).expectNext("John").verifyComplete();
    }

    @Test
    void map_shouldTransformValue() {
        Mono<String> name = Mono.just("John");
        Mono<String> uppercaseName = name.map(String::toUpperCase);

        StepVerifier.create(uppercaseName).expectNext("JOHN").verifyComplete();
    }

    @Test
    void filter_shouldEmitValueWhenConditionMatches() {
        Mono<String> name = Mono.just("John");
        Mono<String> result = name.filter(value -> value.startsWith("J"));

        StepVerifier.create(result).expectNext("John").verifyComplete();
    }

    @Test
    void filter_shouldCompleteEmptyWhenConditionDoesNotMatch() {
        Mono<String> name = Mono.just("John");
        Mono<String> result = name.filter(value -> value.startsWith("P"));

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void empty_shouldCompleteWithoutValue() {
        Mono<String> result = Mono.empty();

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void defaultIfEmpty_shouldProvideFallbackValue() {
        Mono<String> result = Mono.<String>empty().defaultIfEmpty("Unknown");

        StepVerifier.create(result).expectNext("Unknown").verifyComplete();
    }

    @Test
    void map_shouldAllowMultipleOperatorsToBeComposed() {
        Mono<String> result = Mono.just("john").map(String::trim).map(String::toUpperCase)
                .map(name -> "Hello " + name);

        StepVerifier.create(result).expectNext("Hello JOHN").verifyComplete();
    }

    @Test
    void defer_shouldDelayExecutionUntilSubscription() {
        boolean[] executed = { false };

        Mono<String> name = Mono.defer(() -> {
            executed[0] = true;

            return Mono.just("John");
        });

        assertThat(executed[0]).isFalse();

        StepVerifier.create(name).expectNext("John").verifyComplete();

        assertThat(executed[0]).isTrue();
    }

    @Test
    void error_shouldTerminateWithError() {
        Mono<String> result = Mono.error(new IllegalStateException("Something went wrong"));

        StepVerifier.create(result).expectErrorMatches(error ->
                        error instanceof IllegalStateException && error.getMessage().equals("Something went wrong"))
                .verify();
    }
}