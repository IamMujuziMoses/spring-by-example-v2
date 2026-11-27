package com.springbyexample.v2.springaot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.springbyexample.v2.springaot.controller.GreetingController;
import com.springbyexample.v2.springaot.service.GreetingService;

/**
 * Tests application context initialization.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class SpringAotApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(GreetingController.class)).isNotNull();
        assertThat(applicationContext.getBean(GreetingService.class)).isNotNull();
    }
}
