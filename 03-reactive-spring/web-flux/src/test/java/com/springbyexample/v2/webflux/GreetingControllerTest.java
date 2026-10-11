package com.springbyexample.v2.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springbyexample.v2.webflux.controller.GreetingController;
import com.springbyexample.v2.webflux.service.GreetingService;

/**
 * @author Mujuzi Moses
 */
@WebFluxTest(GreetingController.class)
@Import(GreetingService.class)
public class GreetingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void greet_shouldReturnGreetingForRequestedName() {
        // WebTestClient sends a mock HTTP request through the WebFlux infrastructure and verifies the resulting
        // HTTP response.
        webTestClient.get()
                .uri("/api/greetings/Alice").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.message")
                .isEqualTo("Hello, Alice!");
    }

    @Test
    void greetAll_shouldReturnGreetingForEachRequestedName() {
        // Repeated query parameters are bound to a List<String>. The Flux returned by the controller is serialized as
        // a JSON array.
        webTestClient.get()
                .uri("/api/greetings?names=Alice&names=Bob").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$[0].message")
                .isEqualTo("Hello, Alice!")
                .jsonPath("$[1].message")
                .isEqualTo("Hello, Bob!");
    }

    @Test
    void greet_shouldReturnNotFoundForUnknownRoute() {
        // An unmatched URL should not resolve to either greeting endpoint.
        webTestClient.get().uri("/api/unknown").exchange().expectStatus().isNotFound();
    }
}
