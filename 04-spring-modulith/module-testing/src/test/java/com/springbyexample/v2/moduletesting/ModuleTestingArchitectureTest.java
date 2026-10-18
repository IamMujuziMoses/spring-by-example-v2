package com.springbyexample.v2.moduletesting;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author Mujuzi Moses
 */
public class ModuleTestingArchitectureTest {

    @Test
    void shouldVerifyApplicationModuleStructure() {
        ApplicationModules.of(ModuleTestingApplication.class).verify();
    }
}
