package com.springbyexample.v2.reactivetesting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.reactivetesting.model.Greeting;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class GreetingServiceTest {

    private GreetingService greetingService;

    @BeforeEach
    void setUp() {
        greetingService = new GreetingService();
    }

    @Test
    void greet_shouldEmitGreeting() {
        StepVerifier.create(greetingService.greet("Moses")).expectNext(new Greeting("Hello, Moses!")).verifyComplete();
    }

    @Test
    void findAll_shouldEmitAllGreetingsInOrder() {
        StepVerifier.create(greetingService.findAll())
                .expectNext(new Greeting("Hello, Spring!"))
                .expectNext(new Greeting("Hello, Reactor!"))
                .expectNext(new Greeting("Hello, WebFlux!"))
                .verifyComplete();
    }

    @Test
    void shouldCompleteWithoutEmittingUnexpectedValues() {
        StepVerifier.create(Mono.just("Reactive")).expectNext("Reactive").expectComplete().verify();
    }

    @Test
    void shouldVerifyAnErrorSignal() {
        StepVerifier.create(Mono.error(new IllegalStateException("Operation failed")))
                .expectErrorMatches(error -> error instanceof IllegalStateException
                        && error.getMessage().equals("Operation failed")).verify();
    }

    @Test
    void findByName_shouldEmitAnErrorWhenNameIsBlank() {
        StepVerifier.create(greetingService.findByName(" "))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().equals("Name must not be blank")).verify();
    }

    @Test
    void shouldVerifyErrorRecovery() {
        Mono<String> publisher = Mono.<String>error(new IllegalStateException("Operation failed"))
                .onErrorResume(error -> Mono.just("Fallback response"));

        StepVerifier.create(publisher).expectNext("Fallback response").verifyComplete();
    }

    @Test
    void shouldTestTimedPublisherUsingVirtualTime() {
        StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofSeconds(1)).take(3))
                .expectSubscription().thenAwait(Duration.ofSeconds(3)).expectNext(0L, 1L, 2L).verifyComplete();
    }

    @Test
    void shouldVerifyBackpressure() {
        StepVerifier.create(Flux.range(1, 5), 0).expectSubscription()
                .thenRequest(2).expectNext(1, 2).thenRequest(3).expectNext(3, 4, 5).verifyComplete();
    }

    @Test
    void findByName_shouldVerifyMappedGreeting() {
        StepVerifier.create(greetingService.findByName("Reactor"))
                .assertNext(greeting -> assertEquals("Hello, Reactor!", greeting.message()))
                .verifyComplete();
    }
}