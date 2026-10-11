package com.springbyexample.v2.reactiveclients.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.reactiveclients.Post;
import com.springbyexample.v2.reactiveclients.PostClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostClient postClient;

    public PostController(PostClient postClient) {
        this.postClient = postClient;
    }

    @GetMapping
    public Flux<Post> findAll() {
        return postClient.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Post> findById(@PathVariable Long id) {
        return postClient.findById(id);
    }

    @GetMapping("/{id}/title")
    public Mono<String> findTitleById(@PathVariable Long id) {
        return postClient.findTitleById(id);
    }
}
