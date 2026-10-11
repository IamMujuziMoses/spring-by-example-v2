package com.springbyexample.v2.webflux.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.webflux.Greeting;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@Service
public class GreetingService {

    public Mono<Greeting> greet(String name) {
        return Mono.just(createGreeting(name));
    }

    public Flux<Greeting> greetAll(List<String> names) {
        return Flux.fromIterable(names).map(this::createGreeting);
    }

    private Greeting createGreeting(String name) {
        return new Greeting("Hello, " + name + "!");
    }
}
