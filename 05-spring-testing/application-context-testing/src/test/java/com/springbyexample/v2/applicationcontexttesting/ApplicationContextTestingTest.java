package com.springbyexample.v2.applicationcontexttesting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Tests Spring application context configuration.
 *
 * @author Mujuzi Moses
 */
public class ApplicationContextTestingTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(GreetingConfiguration.class);

    @Test
    void shouldLoadGreetingServiceWhenPropertyIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(GreetingService.class);
            assertThat(context.getBean(GreetingService.class).greet("Moses"))
                    .isEqualTo("Hello, Moses!");
        });
    }

    @Test
    void shouldLoadsGreetingServiceWhenPropertyIsEnabled() {
        contextRunner.withPropertyValues("greeting.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(GreetingService.class);
                });
    }

    @Test
    void shouldNotLoadGreetingServiceWhenPropertyIsDisabled() {
        contextRunner.withPropertyValues("greeting.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(GreetingService.class);
                });
    }
}
