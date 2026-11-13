package com.springbyexample.v2.servicediscovery.discoveryserver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.netflix.eureka.EurekaServerContext;

/**
 * Tests the Eureka Discovery Server application.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.register-with-eureka=false", "eureka.client.fetch-registry=false"})
public class DiscoveryServerApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldStartDiscoveryServer() {
        assertThat(applicationContext.getBean(EurekaServerContext.class)).isNotNull();
    }

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void eurekaServerContext_shouldBeAvailable() {
        assertThat(applicationContext.getBean(EurekaServerContext.class))
                .isNotNull();
    }
}

