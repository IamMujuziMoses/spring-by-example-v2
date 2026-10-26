package com.springbyexample.v2.springcontextcaching;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Demonstrates using the same configuration from a second test class.
 *
 * @author Mujuzi Moses
 */
@SpringJUnitConfig(SharedConfiguration.class)
public class SecondContextTest {

    @Autowired
    private SharedConfiguration.ContextMarker contextMarker;

    @Test
    void shouldLoadSharedContext() {
        assertNotNull(contextMarker);
    }
}
