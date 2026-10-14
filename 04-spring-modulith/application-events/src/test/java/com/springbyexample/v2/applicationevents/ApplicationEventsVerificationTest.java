package com.springbyexample.v2.applicationevents;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * @author Mujuzi Moses
 */
public class ApplicationEventsVerificationTest {

    private final ApplicationModules modules = ApplicationModules.of(ApplicationEventsApplication.class);

    @Test
    void shouldVerifyApplicationModuleStructure() {
        modules.verify();
    }
}

