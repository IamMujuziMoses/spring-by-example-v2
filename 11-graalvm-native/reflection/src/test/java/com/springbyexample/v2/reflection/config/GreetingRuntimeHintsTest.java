package com.springbyexample.v2.reflection.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import com.springbyexample.v2.reflection.message.GreetingMessage;
import com.springbyexample.v2.reflection.message.WelcomeMessage;

/**
 * Tests reflection runtime hint registration.
 *
 * @author Mujuzi Moses
 */
public class GreetingRuntimeHintsTest {

    private final GreetingRuntimeHints greetingRuntimeHints = new GreetingRuntimeHints();

    @Test
    void shouldRegisterGreetingMessageReflectionHints() {
        RuntimeHints hints = new RuntimeHints();

        greetingRuntimeHints.registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection().onType(GreetingMessage.class).test(hints)).isTrue();
    }

    @Test
    void shouldRegisterWelcomeMessageReflectionHints() {
        RuntimeHints hints = new RuntimeHints();

        greetingRuntimeHints.registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection().onType(WelcomeMessage.class).test(hints)).isTrue();
    }

    @Test
    void shouldRegisterGreetingMessageConstructorAndMethod() throws Exception {
        RuntimeHints hints = new RuntimeHints();

        greetingRuntimeHints.registerHints(hints, getClass().getClassLoader());

        Constructor<GreetingMessage> constructor = GreetingMessage.class.getDeclaredConstructor();
        Method method = GreetingMessage.class.getMethod("getMessage");

        assertThat(RuntimeHintsPredicates.reflection().onConstructorInvocation(constructor).test(hints)).isTrue();
        assertThat(RuntimeHintsPredicates.reflection().onMethodInvocation(method).test(hints)).isTrue();
    }

}
