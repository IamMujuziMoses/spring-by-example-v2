package com.springbyexample.v2.springaot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.springaot.service.GreetingService;

/**
 * Exposes greeting endpoints for the Spring AOT example.
 *
 * @author Mujuzi Moses
 */
@RestController
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/api/aot/greeting")
    public String greeting(@RequestParam(defaultValue = "Spring AOT") String name) {
        return greetingService.greet(name);
    }
}
