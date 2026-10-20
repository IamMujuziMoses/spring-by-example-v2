package com.springbyexample.v2.springmvctesting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Mujuzi Moses
 */
public class GreetingServiceTest {

    private final GreetingService greetingService = new GreetingService();

    @Test
    void shouldReturnGreetingForProvidedName() {
        assertEquals("Hello, Moses!", greetingService.greet("Moses"));
    }

    @Test
    void shouldReturnGreetingForAnotherName() {
        assertEquals("Hello, Java!", greetingService.greet("Java"));
    }
}
