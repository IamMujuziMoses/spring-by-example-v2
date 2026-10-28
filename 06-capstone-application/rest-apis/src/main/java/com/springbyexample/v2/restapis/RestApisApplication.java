package com.springbyexample.v2.restapis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the REST APIs example.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication(scanBasePackages = { "com.springbyexample.v2.restapis",
        "com.springbyexample.v2.springdataandpersistence" } )
public class RestApisApplication {

    /**
     * Starts the REST APIs application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RestApisApplication.class, args);
    }
}
