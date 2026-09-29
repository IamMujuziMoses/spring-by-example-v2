package com.springbyexample.v2.jwt.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class JwtController {

    @GetMapping("/")
    public String home() {
        return "JWT Example";
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/user")
    public String user(Authentication authentication) {
        return "Authenticated as: " + authentication.getName();
    }
}
