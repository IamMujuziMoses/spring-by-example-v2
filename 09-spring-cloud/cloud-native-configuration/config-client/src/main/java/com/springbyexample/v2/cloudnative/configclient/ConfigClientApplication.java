package com.springbyexample.v2.cloudnative.configclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application class for the Cloud-Native Configuration example.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }
}
