package com.springbyexample.v2.modulararchitecture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Entry point for the modular architecture example.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
@Modulithic
public class ModularArchitectureApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModularArchitectureApplication.class, args);
    }
}
