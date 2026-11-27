package com.springbyexample.v2.springaot.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests the greeting service.
 *
 * @author Mujuzi Moses
 */
public class GreetingServiceTest {

    private final GreetingService greetingService = new GreetingService();

    @Test
    void shouldReturnGreetingForProvidedName() {
        String greeting = greetingService.greet("Mujuzi");

        assertThat(greeting).isEqualTo("Hello, Mujuzi! Welcome to Spring AOT.");
    }

    @Test
    void shouldReturnGreetingForSpringAot() {
        String greeting = greetingService.greet("Spring AOT");

        assertThat(greeting).isEqualTo("Hello, Spring AOT! Welcome to Spring AOT.");
    }
}
