package com.springbyexample.v2.modulararchitecture;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies the application's modular architecture.
 *
 * @author Mujuzi Moses
 */
public class ModularArchitectureVerificationTest {

    @Test
    void applicationModules_shouldRespectModuleBoundaries() {
        ApplicationModules.of(ModularArchitectureApplication.class).verify();
    }
}
