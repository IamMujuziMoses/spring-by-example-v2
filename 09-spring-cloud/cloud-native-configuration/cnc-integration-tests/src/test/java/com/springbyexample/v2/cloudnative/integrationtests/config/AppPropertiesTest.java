package com.springbyexample.v2.cloudnative.integrationtests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.springbyexample.v2.cloudnative.configclient.config.AppProperties;

/**
 * Tests binding of application properties from external configuration.
 *
 * @author Mujuzi Moses
 */
public class AppPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldBindApplicationProperties() {
        contextRunner.withPropertyValues("app.name=Configuration Demo", "app.environment=staging",
                        "app.feature-enabled=true", "app.api-token=example-token")
                .run(context -> {

                    assertTrue(context.isRunning());

                    AppProperties properties = context.getBean(AppProperties.class);

                    assertEquals("Configuration Demo", properties.name());
                    assertEquals("staging", properties.environment());
                    assertTrue(properties.featureEnabled());
                    assertEquals("example-token", properties.apiToken());
                });
    }

    @Test
    void shouldBindFeatureAsDisabled() {
        contextRunner.withPropertyValues("app.feature-enabled=false")
                .run(context -> {

                    AppProperties properties = context.getBean(AppProperties.class);

                    assertFalse(properties.featureEnabled());
                });
    }

    @Test
    void shouldAllowMissingApiToken() {
        contextRunner.withPropertyValues("app.name=Configuration Demo")
                .run(context -> {

                    AppProperties properties =
                            context.getBean(AppProperties.class);

                    assertNull(properties.apiToken());
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AppProperties.class)
    static class TestConfiguration {
    }
}
