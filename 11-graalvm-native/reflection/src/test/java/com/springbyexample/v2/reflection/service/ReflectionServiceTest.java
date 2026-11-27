package com.springbyexample.v2.reflection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Tests reflective message loading.
 *
 * @author Mujuzi Moses
 */
public class ReflectionServiceTest {

    private final ReflectionService reflectionService = new ReflectionService();

    @Test
    void shouldReturnGreetingMessage() {
        String message = reflectionService.getMessage("Greeting");

        assertThat(message).isEqualTo("Hello! Welcome to Spring Reflection.");
    }

    @Test
    void shouldReturnWelcomeMessage() {
        String message = reflectionService.getMessage("Welcome");

        assertThat(message).isEqualTo("Welcome! This example demonstrates reflection in Spring.");
    }

    @Test
    void shouldRejectUnsupportedMessageType() {
        assertThatThrownBy(() -> reflectionService.getMessage("Unknown")).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported message type: Unknown");
    }

    @Test
    void shouldWrapClassNotFoundExceptionInIllegalStateException() {
        String className = "com.example.DoesNotExist";

        assertThatThrownBy(() -> reflectionService.getMessageByClassName(className))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to invoke message implementation: " + className)
                .hasCauseInstanceOf(ClassNotFoundException.class);
    }

    @Test
    void shouldWrapNoSuchMethodExceptionInIllegalStateException() {
        String className = NoDefaultConstructor.class.getName();

        assertThatThrownBy(() -> reflectionService.getMessageByClassName(className))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to invoke message implementation: " + className)
                .hasCauseInstanceOf(NoSuchMethodException.class);
    }

    @Test
    void shouldWrapNoSuchMethodExceptionWhenMessageMethodDoesNotExist() {
        String className = MessageWithoutGetMessage.class.getName();

        assertThatThrownBy(() -> reflectionService.getMessageByClassName(className))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to invoke message implementation: " + className)
                .hasCauseInstanceOf(NoSuchMethodException.class);
    }

    static class NoDefaultConstructor {
        NoDefaultConstructor(String value) {
        }
    }

    static class MessageWithoutGetMessage { }

}
