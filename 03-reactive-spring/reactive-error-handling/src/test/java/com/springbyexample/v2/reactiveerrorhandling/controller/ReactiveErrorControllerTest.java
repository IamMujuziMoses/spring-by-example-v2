package com.springbyexample.v2.reactiveerrorhandling.controller;

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springbyexample.v2.reactiveerrorhandling.model.Product;
import com.springbyexample.v2.reactiveerrorhandling.service.ReactiveErrorService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@WebFluxTest(ReactiveErrorController.class)
public class ReactiveErrorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveErrorService reactiveErrorService;

    @Test
    void shouldReturnAllProducts() {
        given(reactiveErrorService.findAll()).willReturn(Flux.just(
                new Product(1L, "Laptop"),
                new Product(2L, "Keyboard")));

        webTestClient.get().uri("/api/products").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$[0].name").isEqualTo("Laptop")
                .jsonPath("$[1].name").isEqualTo("Keyboard");
    }

    @Test
    void shouldReturnProductById() {
        given(reactiveErrorService.findById(1L)).willReturn(Mono.just(new Product(1L, "Laptop")));

        webTestClient.get().uri("/api/products/1").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Laptop");
    }

    @Test
    void shouldReturnNotFoundForMissingProduct() {
        given(reactiveErrorService.findById(99L)).willReturn(Mono.error(
                new com.springbyexample.v2.reactiveerrorhandling.exception.ProductNotFoundException(99L)));

        webTestClient.get().uri("/api/products/99").exchange().expectStatus().isNotFound().expectBody()
                .jsonPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void shouldReturnBadRequestForInvalidProductId() {
        given(reactiveErrorService.findById(0L)).willReturn(Mono.error(
                new com.springbyexample.v2.reactiveerrorhandling.exception.InvalidProductIdException(0L)));

        webTestClient.get().uri("/api/products/0").exchange().expectStatus().isBadRequest().expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_PRODUCT_ID");
    }

    @Test
    void shouldReturnFallbackResponse() {
        given(reactiveErrorService.demonstrateOnErrorReturn()).willReturn(Mono.just("Fallback response"));

        webTestClient.get().uri("/api/errors/fallback").exchange().expectStatus().isOk().expectBody(String.class)
                .isEqualTo("Fallback response");
    }

    @Test
    void shouldReturnRecoveredResponse() {
        given(reactiveErrorService.demonstrateOnErrorResume())
                .willReturn(Mono.just("Recovered from: Primary operation failed"));

        webTestClient.get().uri("/api/errors/resume").exchange().expectStatus().isOk().expectBody(String.class)
                .isEqualTo("Recovered from: Primary operation failed");
    }

    @Test
    void shouldReturnInternalServerErrorForMappedFailure() {
        given(reactiveErrorService.demonstrateOnErrorMap())
                .willReturn(Mono.error(new com.springbyexample.v2.reactiveerrorhandling.exception
                        .ServiceOperationException("The product operation could not be completed",
                        new IllegalStateException("Database operation failed"))));

        webTestClient.get().uri("/api/errors/mapped").exchange().expectStatus().is5xxServerError().expectBody()
                .jsonPath("$.code").isEqualTo("SERVICE_OPERATION_FAILED");
    }

    @Test
    void shouldReturnInternalServerErrorForObservedFailure() {
        given(reactiveErrorService.demonstrateDoOnError()).willReturn(Mono.error(
                new IllegalStateException("Unexpected reactive failure")));

        webTestClient.get().uri("/api/errors/observed").exchange().expectStatus().is5xxServerError().expectBody()
                .jsonPath("$.code").isEqualTo("INTERNAL_SERVER_ERROR");
    }
}