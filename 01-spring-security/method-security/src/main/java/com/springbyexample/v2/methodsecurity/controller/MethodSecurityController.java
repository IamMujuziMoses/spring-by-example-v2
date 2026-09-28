package com.springbyexample.v2.methodsecurity.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.methodsecurity.service.MethodSecurityService;

/**
 * @author Mujuzi Moses
 */
@RestController
public class MethodSecurityController {

    private final MethodSecurityService methodSecurityService;

    public MethodSecurityController(MethodSecurityService methodSecurityService) {
        this.methodSecurityService = methodSecurityService;
    }

    @GetMapping("/user")
    public String userOperation() {
        return methodSecurityService.userOperation();
    }

    @GetMapping("/admin")
    public String adminOperation() {
        return methodSecurityService.adminOperation();
    }

    @GetMapping("/authenticated")
    public String authenticatedOperation() {
        return methodSecurityService.authenticatedOperation();
    }
}
