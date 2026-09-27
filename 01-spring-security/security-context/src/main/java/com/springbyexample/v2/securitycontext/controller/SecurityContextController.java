package com.springbyexample.v2.securitycontext.controller;

import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityContextController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This endpoint is public.";
    }

    @GetMapping("/authentication")
    public String authentication(Authentication authentication) {
        return "Authenticated as: " + authentication.getName();
    }

    @GetMapping("/security-context")
    public String securityContext() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        assert authentication != null;
        return "Authenticated as: " + authentication.getName();
    }

//    @GetMapping("/details")
//    public String details() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        assert authentication != null;
//        return "Name: " + authentication.getName() + ", Authorities: " + authentication.getAuthorities();
//    }

    @GetMapping("/details")
    public String details() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .sorted().collect(Collectors.joining(", "));

        return "Name: " + authentication.getName() + ", Authorities: [" + authorities + "]";
    }
}
