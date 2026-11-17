package com.springbyexample.v2.resilience.orderclient.config;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

/**
 * @author Mujuzi Moses
 */
@Configuration
public class ResilienceConfiguration {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> orderServiceCircuitBreakerCustomizer() {
        return factory -> factory.configure(
                builder -> builder.circuitBreakerConfig(
                        CircuitBreakerConfig.custom()
                                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                                .slidingWindowSize(4)
                                .minimumNumberOfCalls(4)
                                .failureRateThreshold(50)
                                .waitDurationInOpenState(Duration.ofMillis(200))
                                .permittedNumberOfCallsInHalfOpenState(2)
                                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                                .build()
                ), "orderService");
    }
}