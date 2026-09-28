package com.springbyexample.v2.sessionmanagement.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

/**
 * @author Mujuzi Moses
 */
@RestController
public class SessionManagementController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/session")
    public String session(Authentication authentication, HttpSession session) {
        return "Authenticated as: " + authentication.getName() + ", Session ID: " + session.getId();
    }
}
