package com.springbyexample.v2.springtestcontextframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Mujuzi Moses
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
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
    void shouldInjectGreetingService() {
        assertNotNull(greetingService);
        assertEquals("Hello, Spring!", greetingService.greet("Spring"));
    }
}
