package com.springbyexample.v2.applicationmodules;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author Mujuzi Moses
 */
public class ApplicationModulesDiscoveryTest {

    @Test
    void shouldDiscoverCatalogAndInventoryModules() {
        ApplicationModules modules = ApplicationModules.of(ApplicationModulesApplication.class);

        assertThat(modules.getModuleByName("catalog")).isPresent();
        assertThat(modules.getModuleByName("inventory")).isPresent();
    }

    @Test
    void shouldVerifyApplicationModuleStructure() {
        ApplicationModules.of(ApplicationModulesApplication.class).verify();
    }
}
