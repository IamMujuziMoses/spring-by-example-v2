package com.springbyexample.v2.restartability;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the Spring Batch restartability example.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
@EnableBatchProcessing
@EnableJdbcJobRepository
public class RestartabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestartabilityApplication.class, args);
    }
}
