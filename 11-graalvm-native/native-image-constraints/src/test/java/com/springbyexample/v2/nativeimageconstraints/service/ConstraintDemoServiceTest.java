package com.springbyexample.v2.nativeimageconstraints.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Tests dynamic message loading.
 *
 * @author Mujuzi Moses
 */
public class ConstraintDemoServiceTest {

    private final ConstraintDemoService constraintDemoService = new ConstraintDemoService();

    @Test
    void shouldLoadGreetingMessage() {
        String message = constraintDemoService.loadMessage("Greeting");

        assertThat(message).isEqualTo("Hello! Welcome to GraalVM Native Image Constraints.");
    }

    @Test
    void shouldLoadWelcomeMessage() {
        String message = constraintDemoService.loadMessage("Welcome");

        assertThat(message).isEqualTo("Welcome! This example demonstrates dynamic class loading.");
    }

    @Test
    void shouldRejectUnsupportedMessageType() {
        assertThatThrownBy(() -> constraintDemoService.loadMessage("Unknown"))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Unsupported message type: Unknown");
    }

    @Test
    void shouldThrowClassNotFoundExceptionWhenClassDoesNotExist() {
        assertThatThrownBy(() -> constraintDemoService.instantiateMessage("com.example.DoesNotExist"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    void shouldWrapReflectiveOperationExceptionInIllegalStateException() {
        String className = "com.example.DoesNotExist";

        assertThatThrownBy(() -> constraintDemoService.loadMessageByClassName(className))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to load message implementation: " + className)
                .hasCauseInstanceOf(ClassNotFoundException.class);
    }

    @Test
    void shouldThrowNoSuchMethodExceptionWhenNoDefaultConstructorExists() {
        assertThatThrownBy(() -> constraintDemoService.instantiateMessage(NoDefaultConstructor.class.getName()))
                .isInstanceOf(NoSuchMethodException.class);
    }

    static class NoDefaultConstructor {
        NoDefaultConstructor(String value) {
        }
    }

}
