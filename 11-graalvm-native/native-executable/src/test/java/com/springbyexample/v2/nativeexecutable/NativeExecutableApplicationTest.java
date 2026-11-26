package com.springbyexample.v2.nativeexecutable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Tests application context initialization.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class NativeExecutableApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.containsBean("greetingController")).isTrue();
    }
}
