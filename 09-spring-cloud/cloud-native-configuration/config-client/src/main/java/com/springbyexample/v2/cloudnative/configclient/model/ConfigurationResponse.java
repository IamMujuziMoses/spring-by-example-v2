package com.springbyexample.v2.cloudnative.configclient.model;

/**
 * Response containing the application's effective configuration.
 *
 * @param name               the application display name
 * @param environment        the deployment environment
 * @param featureEnabled     whether the example feature is enabled
 * @param apiTokenConfigured whether an API token has been configured
 *
 * @author Mujuzi Moses
 */
public record ConfigurationResponse(String name, String environment, boolean featureEnabled, boolean apiTokenConfigured) {
}
