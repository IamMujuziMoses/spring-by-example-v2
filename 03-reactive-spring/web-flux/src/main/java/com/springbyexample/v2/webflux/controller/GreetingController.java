package com.springbyexample.v2.webflux.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.webflux.Greeting;
import com.springbyexample.v2.webflux.service.GreetingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/greetings")
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/{name}")
    public Mono<Greeting> greet(@PathVariable String name) {
        return greetingService.greet(name);
    }

    @GetMapping
    public Flux<Greeting> greetAll(@RequestParam(name = "names") List<String> names) {
        return greetingService.greetAll(names);
    }
}
