package com.springbyexample.v2.springcontextcaching;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Demonstrates invalidating a cached application context after a test class.
 *
 * @author Mujuzi Moses
 */
@SpringJUnitConfig(DirtyContextTest.DirtyConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DirtyContextTest {

    @Autowired
    private SharedConfiguration.ContextMarker contextMarker;

    @Test
    void loadsContextThatWillBeInvalidated() {
        assertNotNull(contextMarker);
    }

    @Configuration(proxyBeanMethods = false)
    static class DirtyConfiguration {

        @Bean
        SharedConfiguration.ContextMarker contextMarker() {
            return new SharedConfiguration.ContextMarker();
        }
    }
}
