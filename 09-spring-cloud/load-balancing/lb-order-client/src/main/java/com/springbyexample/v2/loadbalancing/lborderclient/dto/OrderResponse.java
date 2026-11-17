package com.springbyexample.v2.loadbalancing.lborderclient.dto;

/**
 * Represents an order response received from the order service.
 *
 * @param orderId    the requested order identifier
 * @param instanceId the identifier of the service instance handling the request
 *
 * @author Mujuzi Moses
 */
public record OrderResponse(String orderId, String instanceId) {
}
