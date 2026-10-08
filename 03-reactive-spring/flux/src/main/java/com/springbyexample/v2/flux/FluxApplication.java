package com.springbyexample.v2.flux;

import reactor.core.publisher.Flux;

/**
 * @author Mujuzi Moses
 */
public class FluxApplication {

    public static void main(String[] args) {
        Flux<String> names = Flux.just("John", "Jane", "Peter");

        names.map(String::toUpperCase).subscribe(System.out::println);
    }
}
