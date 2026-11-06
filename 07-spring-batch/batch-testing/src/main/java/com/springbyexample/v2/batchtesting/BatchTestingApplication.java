package com.springbyexample.v2.batchtesting;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application for the Spring Batch Testing example.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
@EnableBatchProcessing
@EnableJdbcJobRepository
public class BatchTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchTestingApplication.class, args);
    }
}
