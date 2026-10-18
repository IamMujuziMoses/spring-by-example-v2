package com.springbyexample.v2.moduledocumentation;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * @author Mujuzi Moses
 */
public class ModuleDocumentationTest {

    @Test
    void shouldGenerateModuleDocumentation() {
        var modules = ApplicationModules.of(ModuleDocumentationApplication.class).verify();

        new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml().writeModuleCanvases()
                .writeAggregatingDocument();
    }
}
