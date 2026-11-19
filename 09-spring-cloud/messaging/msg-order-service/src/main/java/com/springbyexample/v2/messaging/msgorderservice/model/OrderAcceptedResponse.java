package com.springbyexample.v2.messaging.msgorderservice.model;

/**
 * Represents the acknowledgement returned after accepting an order.
 *
 * @param orderId the order identifier
 * @param status  the order submission status
 *
 * @author Mujuzi Moses
 */
public record OrderAcceptedResponse(String orderId, String status) {
}
