package com.springbyexample.v2.springcontextcaching;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Demonstrates loading a separate context with a different configuration.
 *
 * @author Mujuzi Moses
 */
@SpringJUnitConfig(AlternativeContextTest.AlternativeConfiguration.class)
public class AlternativeContextTest {

    @Autowired
    private SharedConfiguration.ContextMarker contextMarker;

    @Test
    void shouldLoadAlternativeContext() {
        assertNotNull(contextMarker);
    }

    @Configuration(proxyBeanMethods = false)
    static class AlternativeConfiguration {

        @Bean
        SharedConfiguration.ContextMarker contextMarker() {
            return new SharedConfiguration.ContextMarker();
        }
    }
}
