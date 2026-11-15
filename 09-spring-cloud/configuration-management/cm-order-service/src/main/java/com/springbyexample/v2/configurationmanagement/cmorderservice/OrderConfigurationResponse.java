package com.springbyexample.v2.configurationmanagement.cmorderservice;

/**
 * Response containing the effective Order Service configuration.
 *
 * @author Mujuzi Moses
 */
public record OrderConfigurationResponse(String message, int maxItems, String currency, boolean expeditedEnabled) {
}
