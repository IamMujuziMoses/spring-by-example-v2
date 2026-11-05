package com.springbyexample.v2.jobexecution;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the Job Execution example.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
@EnableBatchProcessing
@EnableJdbcJobRepository
public class JobExecutionApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobExecutionApplication.class, args);
    }
}
