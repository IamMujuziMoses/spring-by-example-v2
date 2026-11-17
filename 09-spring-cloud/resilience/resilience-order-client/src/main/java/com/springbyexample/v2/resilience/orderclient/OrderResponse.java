package com.springbyexample.v2.resilience.orderclient;

/**
 * @author Mujuzi Moses
 */
public record OrderResponse(String orderId, String status, String instanceId, boolean fallback, String message) {
}
