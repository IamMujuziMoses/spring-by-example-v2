package com.springbyexample.v2.springsecurity.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides endpoints for demonstrating role-based access control.
 *
 * @author Mujuzi Moses
 */
@RestController
public class SecurityDemoController {

    @GetMapping("/api/public")
    public Map<String, String> publicResource() {
        return Map.of("message", "This resource is publicly accessible.");
    }

    @GetMapping("/api/user/profile")
    public Map<String, String> userProfile() {
        return Map.of("message", "You have accessed the user profile.", "access", "USER");
    }

    @GetMapping("/api/admin/reports")
    public Map<String, String> adminReports() {
        return Map.of("message", "You have accessed the administrator reports.", "access", "ADMIN");
    }
}
