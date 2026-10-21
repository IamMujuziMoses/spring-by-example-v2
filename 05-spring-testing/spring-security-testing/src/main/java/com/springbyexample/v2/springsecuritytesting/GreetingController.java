package com.springbyexample.v2.springsecuritytesting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class GreetingController {

    @GetMapping("/public")
    public String publicGreeting() {
        return "Hello, everyone!";
    }

    @GetMapping("/greetings")
    public String greeting() {
        return "Hello, authenticated user!";
    }

    @GetMapping("/admin")
    public String adminGreeting() {
        return "Hello, administrator!";
    }
}
