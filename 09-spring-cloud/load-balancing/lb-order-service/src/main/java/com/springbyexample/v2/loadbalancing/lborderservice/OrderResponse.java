package com.springbyexample.v2.loadbalancing.lborderservice;

/**
 * Represents an order response returned by an order service instance.
 *
 * @param orderId    the requested order identifier
 * @param instanceId the identifier of the service instance handling the request
 *
 * @author Mujuzi Moses
 */
public record OrderResponse(String orderId, String instanceId) {
}
