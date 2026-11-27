package com.springbyexample.v2.reflection.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.reflection.service.ReflectionService;

/**
 * Exposes reflection demonstration endpoints.
 *
 * @author Mujuzi Moses
 */
@RestController
public class ReflectionController {

    private final ReflectionService reflectionService;

    public ReflectionController(ReflectionService reflectionService) {
        this.reflectionService = reflectionService;
    }

    @GetMapping("/api/native/reflection/message")
    public String getMessage(@RequestParam(defaultValue = "Greeting") String type) {
        return reflectionService.getMessage(type);
    }
}
