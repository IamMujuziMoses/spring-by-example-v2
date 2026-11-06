package com.springbyexample.v2.skipandretry;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application demonstrating Spring Batch skip and retry processing.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
@EnableBatchProcessing
@EnableJdbcJobRepository
public class SkipAndRetryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkipAndRetryApplication.class, args);
    }
}
