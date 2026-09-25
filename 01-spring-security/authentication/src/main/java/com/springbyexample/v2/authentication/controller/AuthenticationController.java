package com.springbyexample.v2.authentication.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class AuthenticationController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/authenticated")
    public String authenticatedEndpoint(Authentication authentication) {
        return "Authenticated as: " + authentication.getName();
    }
}
