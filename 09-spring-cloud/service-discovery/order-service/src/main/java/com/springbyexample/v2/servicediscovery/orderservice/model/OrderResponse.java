package com.springbyexample.v2.servicediscovery.orderservice.model;

/**
 * Represents an order response returned by the Order Service.
 *
 * @param orderId order identifier
 * @param status  order status
 * @param service service name
 *
 * @author Mujuzi Moses
 */
public record OrderResponse(String orderId, String status, String service) {
}
