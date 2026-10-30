package com.springbyexample.v2.integrationbetweenspringtechnologies;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies the application module structure.
 *
 * @author Mujuzi Moses
 */
public class ApplicationModuleTest {

    @Test
    void verifiesApplicationModuleStructure() {
        ApplicationModules.of(IntegrationApplication.class).verify();
    }
}