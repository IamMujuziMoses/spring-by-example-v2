package com.springbyexample.v2.reactiveclients;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Mujuzi Moses
 */
public class WebClientConfigurationTest {

    @Test
    void shouldConfigureWebClientWithProvidedBaseUrl() {
        String baseUrl = "https://posts.example";

        WebClient.Builder builder = WebClient.builder().exchangeFunction(request -> {
                    assertThat(request.url()).isEqualTo(URI.create(baseUrl + "/posts"));

                    return Mono.just(ClientResponse.create(HttpStatus.OK)
                            .header("Content-Type", "text/plain")
                            .body("ok")
                            .build());
                });

        WebClient webClient = new WebClientConfiguration().postWebClient(builder, baseUrl);

        StepVerifier.create(webClient.get().uri("/posts").retrieve().bodyToMono(String.class))
                .expectNext("ok")
                .verifyComplete();
    }
}
