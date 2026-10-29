package com.springbyexample.v2.modulararchitecture.audit.internal;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.modulararchitecture.audit.AuditEntry;
import com.springbyexample.v2.modulararchitecture.audit.AuditService;

/**
 * Exposes the application audit log through a REST endpoint.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditEntry> getAuditEntries() {
        return auditService.getEntries();
    }
}
