package com.springbyexample.v2.reactivepipelines;

import reactor.core.publisher.Flux;

/**
 * @author Mujuzi Moses
 */
public class ReactivePipelinesApplication {

    public static void main(String[] args) {
        Flux<String> result = Flux.just("spring", "reactor", "webflux", "data")
                .filter(value -> value.length() > 6)
                .map(String::toUpperCase)
                .map(value -> "Topic: " + value);

        result.subscribe(System.out::println);
    }
}
