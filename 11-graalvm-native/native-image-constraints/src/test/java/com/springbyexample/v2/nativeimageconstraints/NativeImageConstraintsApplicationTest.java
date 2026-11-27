package com.springbyexample.v2.nativeimageconstraints;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import com.springbyexample.v2.nativeimageconstraints.controller.ConstraintController;
import com.springbyexample.v2.nativeimageconstraints.service.ConstraintDemoService;

/**
 * Tests application context initialization.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class NativeImageConstraintsApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(ConstraintController.class)).isNotNull();
        assertThat(applicationContext.getBean(ConstraintDemoService.class)).isNotNull();
    }
}
