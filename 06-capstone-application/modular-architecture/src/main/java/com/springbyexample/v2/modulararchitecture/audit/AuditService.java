package com.springbyexample.v2.modulararchitecture.audit;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Service;

/**
 * Maintains the in-memory application audit log.
 *
 * @author Mujuzi Moses
 */
@Service
public class AuditService {

    private final ConcurrentLinkedQueue<AuditEntry> entries = new ConcurrentLinkedQueue<>();

    public void record(AuditEntry entry) {
        entries.add(entry);
    }

    public List<AuditEntry> getEntries() {
        return entries.stream().sorted(Comparator.comparing(AuditEntry::occurredAt)).toList();
    }
}
