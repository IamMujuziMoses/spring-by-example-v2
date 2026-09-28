package com.springbyexample.v2.methodsecurity.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Mujuzi Moses
 */
@Service
public class MethodSecurityService {

    @PreAuthorize("hasRole('USER')")
    public String userOperation() {
        return "User operation executed.";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String adminOperation() {
        return "Admin operation executed.";
    }

    @PreAuthorize("isAuthenticated()")
    public String authenticatedOperation() {
        return "Authenticated operation executed.";
    }
}
