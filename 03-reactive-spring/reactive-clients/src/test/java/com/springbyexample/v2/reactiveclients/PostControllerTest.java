package com.springbyexample.v2.reactiveclients;

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springbyexample.v2.reactiveclients.controller.PostController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */

@WebFluxTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PostClient postClient;

    @Test
    void shouldReturnAllPosts() {
        given(postClient.findAll()).willReturn(Flux.just(
                new Post(1L, 1L, "First post", "First body"),
                new Post(2L, 2L, "Second post", "Second body")));

        webTestClient.get()
                .uri("/api/posts").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].title").isEqualTo("First post")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].title").isEqualTo("Second post");
    }

    @Test
    void shouldReturnPostById() {
        given(postClient.findById(1L))
                .willReturn(Mono.just(new Post(1L, 1L, "Example post", "Example body")));

        webTestClient.get()
                .uri("/api/posts/1").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Example post")
                .jsonPath("$.body").isEqualTo("Example body");
    }

    @Test
    void shouldReturnPostTitle() {
        given(postClient.findTitleById(1L)).willReturn(Mono.just("Example post"));

        webTestClient.get()
                .uri("/api/posts/1/title").exchange().expectStatus().isOk().expectBody(String.class)
                .isEqualTo("Example post");
    }
}
