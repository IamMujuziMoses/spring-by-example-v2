package com.springbyexample.v2.cloudnative.configclient.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.cloudnative.configclient.config.AppProperties;
import com.springbyexample.v2.cloudnative.configclient.model.ConfigurationResponse;

/**
 * Exposes the application's effective external configuration.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/configuration")
public class ConfigurationController {

    private final AppProperties appProperties;

    public ConfigurationController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping
    public ConfigurationResponse getConfiguration() {
        return new ConfigurationResponse(appProperties.name(), appProperties.environment(), appProperties.featureEnabled(),
                appProperties.apiToken() != null && !appProperties.apiToken().isBlank());
    }
}
