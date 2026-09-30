package com.springbyexample.v2.securitytesting.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.securitytesting.service.SecurityTestingService;

/**
 * @author Mujuzi Moses
 */
@RestController
public class SecurityTestingController {

    private final SecurityTestingService securityTestingService;

    public SecurityTestingController(SecurityTestingService securityTestingService) {
        this.securityTestingService = securityTestingService;
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/user")
    public String userEndpoint(Authentication authentication) {
        return "Authenticated as: " + authentication.getName();
    }

    @GetMapping("/admin")
    public String adminEndpoint() {
        return "Admin endpoint accessed.";
    }

    @PostMapping("/transfer")
    public String transfer() {
        return "Transfer completed.";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication) {
        return "Profile: " + authentication.getName();
    }

    @GetMapping("/user-operation")
    public String userOperation() {
        return securityTestingService.userOperation();
    }

    @GetMapping("/admin-operation")
    public String adminOperation() {
        return securityTestingService.adminOperation();
    }

    @GetMapping("/authenticated-operation")
    public String authenticatedOperation() {
        return securityTestingService.authenticatedOperation();
    }
}
