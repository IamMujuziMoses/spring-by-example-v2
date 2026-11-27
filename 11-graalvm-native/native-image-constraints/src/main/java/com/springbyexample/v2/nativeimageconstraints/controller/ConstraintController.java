package com.springbyexample.v2.nativeimageconstraints.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.nativeimageconstraints.service.ConstraintDemoService;

/**
 * Exposes endpoints for demonstrating Native Image constraints.
 *
 * @author Mujuzi Moses
 */
@RestController
public class ConstraintController {

    private final ConstraintDemoService constraintDemoService;

    public ConstraintController(ConstraintDemoService constraintDemoService) {
        this.constraintDemoService = constraintDemoService;
    }

    @GetMapping("/api/native/constraints/message")
    public String getMessage(@RequestParam(defaultValue = "Greeting") String type) {
        return constraintDemoService.loadMessage(type);
    }
}
