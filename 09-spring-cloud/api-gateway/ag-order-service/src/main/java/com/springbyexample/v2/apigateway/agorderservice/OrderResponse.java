package com.springbyexample.v2.apigateway.agorderservice;

/**
 * Response returned by the Order Service.
 *
 * @param service the name of the backend service
 * @param orderId the requested order identifier
 * @param message a descriptive response message
 *
 * @author Mujuzi Moses
 */
public record OrderResponse(String service, String orderId, String message) {
}
