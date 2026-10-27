package com.springbyexample.v2.springbootapplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Tests the Spring Boot application setup.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class SpringBootApplicationSetupTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadApplicationContext() {
        assertNotNull(applicationContext);
    }

    @Test
    void shouldRegisterApplicationClassAsBean() {
        assertNotNull(applicationContext.getBean(SpringBootApplicationSetup.class));
    }
}
