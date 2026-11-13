package com.springbyexample.v2.servicediscovery.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Order Service.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
public class OrderServiceApplication {

    /**
     * Starts the Order Service.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
