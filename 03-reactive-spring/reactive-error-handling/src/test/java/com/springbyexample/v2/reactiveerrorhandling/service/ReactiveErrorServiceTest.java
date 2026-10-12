package com.springbyexample.v2.reactiveerrorhandling.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.reactiveerrorhandling.exception.InvalidProductIdException;
import com.springbyexample.v2.reactiveerrorhandling.exception.ProductNotFoundException;
import com.springbyexample.v2.reactiveerrorhandling.exception.ServiceOperationException;

import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class ReactiveErrorServiceTest {

    private ReactiveErrorService service;

    @BeforeEach
    void setUp() {
        service = new ReactiveErrorService();
    }

    @Test
    void shouldFindAllProducts() {
        StepVerifier.create(service.findAll()).expectNextCount(3).verifyComplete();
    }

    @Test
    void shouldFindProductById() {
        StepVerifier.create(service.findById(1L))
                .assertNext(product -> {
                    assertThat(product.id()).isEqualTo(1L);
                    assertThat(product.name()).isEqualTo("Laptop");
                }).verifyComplete();
    }

    @Test
    void shouldReturnErrorForInvalidProductId() {
        StepVerifier.create(service.findById(0L)).expectError(InvalidProductIdException.class).verify();
    }

    @Test
    void shouldReturnErrorWhenProductDoesNotExist() {
        StepVerifier.create(service.findById(99L)).expectError(ProductNotFoundException.class).verify();
    }

    @Test
    void shouldReturnFallbackValueOnError() {
        StepVerifier.create(service.demonstrateOnErrorReturn()).expectNext("Fallback response").verifyComplete();
    }

    @Test
    void shouldResumeWithAlternativePublisher() {
        StepVerifier.create(service.demonstrateOnErrorResume()).expectNext("Recovered from: Primary operation failed")
                .verifyComplete();
    }

    @Test
    void shouldMapErrorToServiceOperationException() {
        StepVerifier.create(service.demonstrateOnErrorMap())
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ServiceOperationException.class);
                    assertThat(error.getCause())
                            .isInstanceOf(IllegalStateException.class);
                }).verify();
    }

    @Test
    void shouldObserveErrorAndPropagateIt() {
        StepVerifier.create(service.demonstrateDoOnError())
                .expectErrorMatches(error -> error instanceof IllegalStateException && error.getMessage()
                        .equals("Unexpected reactive failure")).verify();
    }
}
