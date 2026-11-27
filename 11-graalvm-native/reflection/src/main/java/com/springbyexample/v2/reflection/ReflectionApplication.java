package com.springbyexample.v2.reflection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

import com.springbyexample.v2.reflection.config.GreetingRuntimeHints;

/**
 * Main entry point for the Spring Reflection example.
 *
 * @author Mujuzi Moses
 */
@SpringBootApplication
@ImportRuntimeHints(GreetingRuntimeHints.class)
public class ReflectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReflectionApplication.class, args);
    }
}
