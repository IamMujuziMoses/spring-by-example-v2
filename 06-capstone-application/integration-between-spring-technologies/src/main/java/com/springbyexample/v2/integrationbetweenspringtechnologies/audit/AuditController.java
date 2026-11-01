package com.springbyexample.v2.integrationbetweenspringtechnologies.audit;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes read-only audit entry endpoints.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditRepository auditRepository;

    public AuditController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping
    public List<AuditEntry> getAuditEntries() {
        return auditRepository.findAllByOrderByOccurredAtDesc();
    }
}