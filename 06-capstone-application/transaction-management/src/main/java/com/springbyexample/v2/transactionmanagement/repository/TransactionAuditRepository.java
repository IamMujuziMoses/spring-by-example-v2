package com.springbyexample.v2.transactionmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springbyexample.v2.transactionmanagement.domain.TransactionAudit;

/**
 * Provides persistence operations for transaction audit records.
 *
 * @author Mujuzi Moses
 */
public interface TransactionAuditRepository extends JpaRepository<TransactionAudit, Long> {
}

