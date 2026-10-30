package com.springbyexample.v2.integrationbetweenspringtechnologies.audit.internal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.springbyexample.v2.integrationbetweenspringtechnologies.audit.AuditEntry;
import com.springbyexample.v2.integrationbetweenspringtechnologies.audit.AuditRepository;
import com.springbyexample.v2.integrationbetweenspringtechnologies.ordering.OrderPlacedEvent;

/**
 * Records audit entries after successful order placement.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderAuditListener {

    private final AuditRepository auditRepository;

    public OrderAuditListener(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(OrderPlacedEvent event) {
        AuditEntry entry = new AuditEntry("ORDER_PLACED", event.orderId(), "Order placed by "
                + event.customerUsername() + " with total amount " + event.totalAmount(), event.occurredAt());

        auditRepository.save(entry);
    }
}
