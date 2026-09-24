package com.springbyexample.v2.basicspringsecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class BasicSecurityController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/private")
    public String privateEndpoint() {
        return "This endpoint is protected.";
    }
}
