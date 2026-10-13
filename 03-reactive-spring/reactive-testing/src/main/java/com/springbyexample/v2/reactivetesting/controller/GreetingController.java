package com.springbyexample.v2.reactivetesting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.reactivetesting.model.Greeting;
import com.springbyexample.v2.reactivetesting.service.GreetingService;

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

    @GetMapping
    public Flux<Greeting> findAll() {
        return greetingService.findAll();
    }

    @GetMapping("/personal")
    public Mono<Greeting> greet(
            @RequestParam(defaultValue = "Spring") String name) {
        return greetingService.greet(name);
    }

    @GetMapping("/{name}")
    public Mono<Greeting> findByName(@PathVariable String name) {
        return greetingService.findByName(name);
    }
}
