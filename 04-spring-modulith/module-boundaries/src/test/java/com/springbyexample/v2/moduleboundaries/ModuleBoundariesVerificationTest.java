package com.springbyexample.v2.moduleboundaries;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author Mujuzi Moses
 */
public class ModuleBoundariesVerificationTest {

    private final ApplicationModules modules = ApplicationModules.of(ModuleBoundariesApplication.class);

    @Test
    void shouldVerifyModuleBoundaries() {
        modules.verify();
    }
}
