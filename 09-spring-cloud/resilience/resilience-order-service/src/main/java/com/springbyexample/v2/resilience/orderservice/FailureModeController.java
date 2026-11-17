package com.springbyexample.v2.resilience.orderservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/admin/failure-mode")
public class FailureModeController {

    private final FailureModeService failureModeService;

    public FailureModeController(FailureModeService failureModeService) {
        this.failureModeService = failureModeService;
    }

    @PutMapping
    public ResponseEntity<Void> setFailureMode(@RequestParam("enabled") boolean enabled) {
        failureModeService.setFailureEnabled(enabled);
        return ResponseEntity.noContent().build();
    }

}
