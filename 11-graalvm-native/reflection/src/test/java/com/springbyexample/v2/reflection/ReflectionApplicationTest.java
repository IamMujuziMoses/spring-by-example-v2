package com.springbyexample.v2.reflection;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.springbyexample.v2.reflection.controller.ReflectionController;
import com.springbyexample.v2.reflection.service.ReflectionService;

/**
 * Tests application context initialization.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ReflectionApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(ReflectionController.class)).isNotNull();
        assertThat(applicationContext.getBean(ReflectionService.class)).isNotNull();
    }
}
