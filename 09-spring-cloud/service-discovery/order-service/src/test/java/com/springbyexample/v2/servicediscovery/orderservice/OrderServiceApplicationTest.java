package com.springbyexample.v2.servicediscovery.orderservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests the Order Service application.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class OrderServiceApplicationTest {

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }
}

