package com.springbyexample.v2.reactivetesting.controller;

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springbyexample.v2.reactivetesting.model.Greeting;
import com.springbyexample.v2.reactivetesting.service.GreetingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@WebFluxTest(GreetingController.class)
public class GreetingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GreetingService greetingService;

    @Test
    void findAll_shouldReturnAllGreetings() {
        given(greetingService.findAll()).willReturn(Flux.just(
                new Greeting("Hello, Spring!"),
                new Greeting("Hello, Reactor!")
        ));

        webTestClient.get()
                .uri("/api/greetings").exchange().expectStatus().isOk().expectHeader()
                .contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON).expectBody()
                .jsonPath("$[0].message")
                .isEqualTo("Hello, Spring!")
                .jsonPath("$[1].message")
                .isEqualTo("Hello, Reactor!");
    }

    @Test
    void greet_shouldReturnPersonalGreeting() {
        given(greetingService.greet("Moses")).willReturn(Mono.just(new Greeting("Hello, Moses!")));

        webTestClient.get()
                .uri("/api/greetings/personal?name=Moses").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.message")
                .isEqualTo("Hello, Moses!");
    }

    @Test
    void greet_shouldUseDefaultNameWhenQueryParameterIsMissing() {
        given(greetingService.greet("Spring")).willReturn(Mono.just(new Greeting("Hello, Spring!")));

        webTestClient.get()
                .uri("/api/greetings/personal").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.message")
                .isEqualTo("Hello, Spring!");
    }

    @Test
    void findByName_shouldReturnGreetingForName() {
        given(greetingService.findByName("Reactor")).willReturn(Mono.just(new Greeting("Hello, Reactor!")));

        webTestClient.get()
                .uri("/api/greetings/Reactor").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.message")
                .isEqualTo("Hello, Reactor!");
    }

    @Test
    void findByName_shouldReturnServerErrorWhenPublisherFails() {
        given(greetingService.findByName("Failure")).willReturn(Mono.error(new IllegalStateException("Operation failed")));

        webTestClient.get().uri("/api/greetings/Failure").exchange().expectStatus().is5xxServerError();
    }
}
