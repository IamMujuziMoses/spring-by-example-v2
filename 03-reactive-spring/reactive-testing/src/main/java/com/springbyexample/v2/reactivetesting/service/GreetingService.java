package com.springbyexample.v2.reactivetesting.service;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.reactivetesting.model.Greeting;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@Service
public class GreetingService {

    public Mono<Greeting> greet(String name) {
        return Mono.just(new Greeting("Hello, " + name + "!"));
    }

    public Flux<Greeting> findAll() {
        return Flux.just(
                new Greeting("Hello, Spring!"),
                new Greeting("Hello, Reactor!"),
                new Greeting("Hello, WebFlux!")
        );
    }

    public Mono<Greeting> findByName(String name) {
        return Mono.just(name).filter(value -> !value.isBlank())
                .map(value -> new Greeting("Hello, " + value + "!"))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Name must not be blank")));
    }
}
