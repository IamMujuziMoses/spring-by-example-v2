package com.springbyexample.v2.applicationtesting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes REST endpoints for greeting operations.
 *
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
    public Greeting greet(@RequestParam(defaultValue = "Spring") String name) {
        return greetingService.greet(name);
    }
}
