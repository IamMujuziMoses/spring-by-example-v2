package com.springbyexample.v2.reactiveclients;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class PostClientTest {

    @Test
    void findById_shouldDecodeResponseIntoPost() {
        // The exchange function simulates the remote HTTP response, allowing the client to be tested without a live API.
        PostClient postClient = createClient("""
                {
                  "userId": 1,
                  "id": 1,
                  "title": "Example title",
                  "body": "Example body"
                }
                """);

        StepVerifier.create(postClient.findById(1L))
                .assertNext(post -> {
                    assertThat(post.id()).isEqualTo(1L);
                    assertThat(post.userId()).isEqualTo(1L);
                    assertThat(post.title()).isEqualTo("Example title");
                    assertThat(post.body()).isEqualTo("Example body");
                })
                .verifyComplete();
    }

    @Test
    void findAll_shouldDecodeResponseIntoPostSequence() {
        // A JSON array is decoded into a Flux of individual Post values.
        PostClient postClient = createClient("""
                [
                  {
                    "userId": 1,
                    "id": 1,
                    "title": "First post",
                    "body": "First body"
                  },
                  {
                    "userId": 2,
                    "id": 2,
                    "title": "Second post",
                    "body": "Second body"
                  }
                ]
                """);

        StepVerifier.create(postClient.findAll())
                .assertNext(post -> {
                    assertThat(post.id()).isEqualTo(1L);
                    assertThat(post.title()).isEqualTo("First post");
                })
                .assertNext(post -> {
                    assertThat(post.id()).isEqualTo(2L);
                    assertThat(post.title()).isEqualTo("Second post");
                })
                .verifyComplete();
    }

    @Test
    void findTitleById_shouldTransformPostIntoTitle() {
        // The client composes the lookup publisher with map() instead of making a separate HTTP request for the title.
        PostClient postClient = createClient("""
                {
                  "userId": 1,
                  "id": 1,
                  "title": "Reactive composition",
                  "body": "Example body"
                }
                """);

        StepVerifier.create(postClient.findTitleById(1L)).expectNext("Reactive composition").verifyComplete();
    }

    @Test
    void findById_shouldSignalErrorForNotFoundResponse() {
        // retrieve() converts a 404 response into a reactive error signal by default.
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()))
                .build();

        PostClient postClient = new PostClient(webClient);

        StepVerifier.create(postClient.findById(999L)).expectError(WebClientResponseException.NotFound.class)
                .verify();
    }

    private PostClient createClient(String responseBody) {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(request -> Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(responseBody)
                                .build()))
                .build();

        return new PostClient(webClient);
    }
}
