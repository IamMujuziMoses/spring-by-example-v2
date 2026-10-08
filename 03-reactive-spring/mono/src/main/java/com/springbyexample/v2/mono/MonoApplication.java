package com.springbyexample.v2.mono;

import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
public class MonoApplication {

    public static void main(String[] args) {
        Mono<String> name = Mono.just("John");

        name.map(String::toUpperCase).subscribe(System.out::println);
    }
}
