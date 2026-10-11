package com.springbyexample.v2.reactiveclients;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@Component
public class PostClient {

    private final WebClient webClient;

    public PostClient(WebClient postWebClient) {
        this.webClient = postWebClient;
    }

    public Flux<Post> findAll() {
        return webClient.get()
                .uri("/posts").retrieve().bodyToFlux(Post.class);
    }

    public Mono<Post> findById(Long id) {
        return webClient.get()
                .uri("/posts/{id}", id).retrieve().bodyToMono(Post.class);
    }

    public Mono<String> findTitleById(Long id) {
        return findById(id).map(Post::title);
    }
}
