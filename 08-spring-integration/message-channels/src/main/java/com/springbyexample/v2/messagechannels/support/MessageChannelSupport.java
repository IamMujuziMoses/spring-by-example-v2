package com.springbyexample.v2.messagechannels.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.springbyexample.v2.messagechannels.domain.OrderMessage;

/**
 * Stores messages received by the message channel handlers.
 *
 * @author Mujuzi Moses
 */
@Component
public class MessageChannelSupport {

    private final List<OrderMessage> directMessages = new CopyOnWriteArrayList<>();

    private final List<OrderMessage> auditMessages = new CopyOnWriteArrayList<>();

    private final List<OrderMessage> notificationMessages = new CopyOnWriteArrayList<>();

    /**
     * Records a message received through the direct channel.
     *
     * @param message received order message
     */
    public void recordDirectMessage(OrderMessage message) {
        directMessages.add(message);
    }

    /**
     * Records a message received by the audit subscriber.
     *
     * @param message received order message
     */
    public void recordAuditMessage(OrderMessage message) {
        auditMessages.add(message);
    }

    /**
     * Records a message received by the notification subscriber.
     *
     * @param message received order message
     */
    public void recordNotificationMessage(OrderMessage message) {
        notificationMessages.add(message);
    }

    /**
     * Returns messages received through the direct channel.
     *
     * @return immutable list of direct messages
     */
    public List<OrderMessage> directMessages() {
        return List.copyOf(directMessages);
    }

    /**
     * Returns messages received by the audit subscriber.
     *
     * @return immutable list of audit messages
     */
    public List<OrderMessage> auditMessages() {
        return List.copyOf(auditMessages);
    }

    /**
     * Returns messages received by the notification subscriber.
     *
     * @return immutable list of notification messages
     */
    public List<OrderMessage> notificationMessages() {
        return List.copyOf(notificationMessages);
    }

    /**
     * Clears all recorded messages.
     */
    public void reset() {
        directMessages.clear();
        auditMessages.clear();
        notificationMessages.clear();
    }

}
