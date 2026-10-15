package com.springbyexample.v2.eventexternalization.orders;

import org.springframework.modulith.events.Externalized;

/**
 * @author Mujuzi Moses
 */
@Externalized("orders.completed")
public record OrderCompleted(String orderId) {
}
