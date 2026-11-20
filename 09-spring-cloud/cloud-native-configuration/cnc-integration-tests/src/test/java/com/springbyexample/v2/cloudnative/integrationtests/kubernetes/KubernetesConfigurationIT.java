package com.springbyexample.v2.cloudnative.integrationtests.kubernetes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.web.client.RestClient;

/**
 * Integration tests for configuration loaded from Kubernetes.
 *
 * @author Mujuzi Moses
 */
@EnabledIfSystemProperty(named = "skipCloudNativeConfigurationITs", matches = "false")
public class KubernetesConfigurationIT {

    private static final String CONFIGURATION_ENDPOINT = "/api/configuration";

    @Test
    void shouldExposeConfigurationLoadedFromKubernetes() {
        String baseUrl = System.getProperty("cloudNativeConfigurationBaseUrl", "http://localhost:8080");

        ConfigurationResponse response = RestClient.create(baseUrl).get().uri(CONFIGURATION_ENDPOINT).retrieve()
                .body(ConfigurationResponse.class);

        assertNotNull(response);
        assertEquals("Cloud Native Configuration Demo", response.name());
        assertEquals("staging", response.environment());
        assertTrue(response.featureEnabled());
        assertTrue(response.apiTokenConfigured());
    }

    /**
     * Represents the configuration endpoint's JSON response.
     *
     * @param name               application display name
     * @param environment        deployment environment
     * @param featureEnabled     whether the feature is enabled
     * @param apiTokenConfigured whether an API token is configured
     */
    private record ConfigurationResponse(String name, String environment, boolean featureEnabled,
            boolean apiTokenConfigured) {
    }
}
