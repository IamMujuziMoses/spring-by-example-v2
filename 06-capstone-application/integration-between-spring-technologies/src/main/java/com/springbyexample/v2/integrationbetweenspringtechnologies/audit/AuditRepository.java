package com.springbyexample.v2.integrationbetweenspringtechnologies.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for accessing audit entries.
 *
 * @author Mujuzi Moses
 */
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {

    List<AuditEntry> findAllByOrderByOccurredAtDesc();
}
