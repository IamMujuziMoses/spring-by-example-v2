package com.springbyexample.v2.cloudnative.configclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application properties loaded from external configuration sources.
 *
 * @param name           the application display name
 * @param environment    the deployment environment
 * @param featureEnabled whether the example feature is enabled
 * @param apiToken       the API token loaded from a Kubernetes Secret
 *
 * @author Mujuzi Moses
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String name, String environment, boolean featureEnabled, String apiToken) {
}
