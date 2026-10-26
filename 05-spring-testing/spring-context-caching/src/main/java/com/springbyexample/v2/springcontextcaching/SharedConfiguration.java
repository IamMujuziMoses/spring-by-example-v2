package com.springbyexample.v2.springcontextcaching;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared configuration used by multiple test classes to demonstrate context caching.
 *
 * @author Mujuzi Moses
 */
@Configuration(proxyBeanMethods = false)
public class SharedConfiguration {

    @Bean
    ContextMarker contextMarker() {
        return new ContextMarker();
    }

    /**
     * Marker bean used to verify that tests receive the same application context.
     */
    static class ContextMarker {
    }
}
