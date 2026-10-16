package com.springbyexample.v2.moduleverification;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author Mujuzi Moses
 */
public class ModuleVerificationTest {

    @Test
    void shouldVerifyApplicationModuleStructure() {
        ApplicationModules.of(ModuleVerificationApplication.class).verify();
    }
}
