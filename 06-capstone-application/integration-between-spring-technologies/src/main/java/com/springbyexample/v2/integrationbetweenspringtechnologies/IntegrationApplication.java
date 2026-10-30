package com.springbyexample.v2.integrationbetweenspringtechnologies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Main entry point for the Integration Between Spring Technologies example.
 *
 * <p>This application demonstrates how Spring Boot, Spring Data JPA,
 * Spring Security, transaction management, and Spring Modulith work
 * together in a realistic order management application.</p>
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
@Modulithic
public class IntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationApplication.class, args);
    }
}
