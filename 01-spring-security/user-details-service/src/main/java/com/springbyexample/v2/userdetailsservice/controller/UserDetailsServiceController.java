package com.springbyexample.v2.userdetailsservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class UserDetailsServiceController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/user")
    public String userEndpoint(Authentication authentication) {
        return "Authenticated user: " + authentication.getName();
    }
}
