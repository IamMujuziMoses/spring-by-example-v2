package com.springbyexample.v2.servicediscovery.orderclient.model;

/**
 * Represents an order response received from the Order Service.
 *
 * @param orderId order identifier
 * @param status  order status
 * @param service service name
 *
 * @author Mujuzi Moses
 */
public record OrderResponse(String orderId, String status, String service) {
}