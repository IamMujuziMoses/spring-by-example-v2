package com.springbyexample.v2.configurationmanagement.cmorderservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for order processing.
 *
 * @author Mujuzi Moses
 */
@ConfigurationProperties(prefix = "app.order")
public record OrderProperties(String message, int maxItems, String currency, boolean expeditedEnabled) {
}
