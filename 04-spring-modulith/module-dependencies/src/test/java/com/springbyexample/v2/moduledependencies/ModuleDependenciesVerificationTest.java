package com.springbyexample.v2.moduledependencies;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author Mujuzi Moses
 */
public class ModuleDependenciesVerificationTest {

    private final ApplicationModules modules = ApplicationModules.of(ModuleDependenciesApplication.class);

    @Test
    void verify_shouldVerifyModuleDependencies() {
        modules.verify();
    }
}
