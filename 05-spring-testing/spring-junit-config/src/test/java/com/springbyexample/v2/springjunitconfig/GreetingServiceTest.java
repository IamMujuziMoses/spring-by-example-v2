package com.springbyexample.v2.springjunitconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author Mujuzi Moses
 */
@SpringJUnitConfig(classes = AppConfig.class)
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
