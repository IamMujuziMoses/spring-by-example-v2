package com.springbyexample.v2.springboottest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
public class GreetingServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GreetingService greetingService;

    @Test
    void shouldLoadApplicationContext() {
        assertNotNull(applicationContext);
    }

    @Test
    void shouldRegisterGreetingService() {
        assertNotNull(greetingService);
    }

    @Test
    void shouldReturnGreeting() {
        assertEquals("Hello, Spring!", greetingService.greet("Spring"));
    }
}
