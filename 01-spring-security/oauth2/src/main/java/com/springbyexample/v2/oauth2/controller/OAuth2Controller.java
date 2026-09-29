package com.springbyexample.v2.oauth2.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class OAuth2Controller {

    @GetMapping("/")
    public String home() {
        return "OAuth2 Login Example";
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/user")
    public String user(OAuth2AuthenticationToken authentication) {
        return "Authenticated as: " + authentication.getName();
    }
}
