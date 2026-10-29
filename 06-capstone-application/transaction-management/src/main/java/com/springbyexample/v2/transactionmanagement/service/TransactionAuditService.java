package com.springbyexample.v2.transactionmanagement.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.springbyexample.v2.transactionmanagement.domain.TransactionAudit;
import com.springbyexample.v2.transactionmanagement.repository.TransactionAuditRepository;

/**
 * Records transaction failures in an independent transaction.
 *
 * @author Mujuzi Moses
 */
@Service
public class TransactionAuditService {

    private static final String PROJECT_CREATION_FAILED = "PROJECT_CREATION_FAILED";

    private final TransactionAuditRepository auditRepository;

    public TransactionAuditService(TransactionAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Records a project creation failure independently of the failed operation.
     *
     * @param projectName the name of the project being created
     * @param details     the failure details
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordProjectCreationFailure(String projectName, String details) {

        TransactionAudit audit = new TransactionAudit(PROJECT_CREATION_FAILED, projectName, details, Instant.now());
        auditRepository.save(audit);
    }
}

