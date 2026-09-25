package com.springbyexample.v2.securityfilterchain.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class SecurityFilterChainController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/user")
    public String userEndpoint() {
        return "This endpoint requires the USER role.";
    }

    @GetMapping("/authenticated")
    public String authenticatedEndpoint() {
        return "This endpoint requires authentication.";
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "This endpoint requires the ADMIN role.";
    }
}
