package com.springbyexample.v2.reactiveerrorhandling.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.reactiveerrorhandling.model.Product;
import com.springbyexample.v2.reactiveerrorhandling.service.ReactiveErrorService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api")
public class ReactiveErrorController {

    private final ReactiveErrorService reactiveErrorService;

    public ReactiveErrorController(ReactiveErrorService reactiveErrorService) {
        this.reactiveErrorService = reactiveErrorService;
    }

    @GetMapping("/products")
    public Flux<Product> findAll() {
        return reactiveErrorService.findAll();
    }

    @GetMapping("/products/{id}")
    public Mono<Product> findById(@PathVariable Long id) {
        return reactiveErrorService.findById(id);
    }

    @GetMapping("/errors/fallback")
    public Mono<String> fallback() {
        return reactiveErrorService.demonstrateOnErrorReturn();
    }

    @GetMapping("/errors/resume")
    public Mono<String> resume() {
        return reactiveErrorService.demonstrateOnErrorResume();
    }

    @GetMapping("/errors/mapped")
    public Mono<String> mapped() {
        return reactiveErrorService.demonstrateOnErrorMap();
    }

    @GetMapping("/errors/observed")
    public Mono<String> observed() {
        return reactiveErrorService.demonstrateDoOnError();
    }
}
