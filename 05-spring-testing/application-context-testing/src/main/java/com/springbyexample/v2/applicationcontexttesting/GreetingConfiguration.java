package com.springbyexample.v2.applicationcontexttesting;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the greeting service.
 *
 * @author Mujuzi Moses
 */
@Configuration(proxyBeanMethods = false)
public class GreetingConfiguration {

    /**
     * Registers the greeting service when greeting is enabled.
     *
     * @return the configured greeting service
     */
    @Bean
    @ConditionalOnProperty(prefix = "greeting", name = "enabled", havingValue = "true", matchIfMissing = true)
    public GreetingService greetingService() {
        return new GreetingService();
    }
}
