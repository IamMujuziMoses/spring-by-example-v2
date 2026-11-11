package com.springbyexample.v2.errorchannels;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application for the Spring Integration Error Channels example.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
public class ErrorChannelsApplication {

    /**
     * Starts the Error Channels application.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ErrorChannelsApplication.class, args);
    }
}
