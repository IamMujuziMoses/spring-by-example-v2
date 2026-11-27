package com.springbyexample.v2.reflection.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import com.springbyexample.v2.reflection.message.GreetingMessage;
import com.springbyexample.v2.reflection.message.WelcomeMessage;

/**
 * Registers reflection hints for message implementations.
 *
 * @author Mujuzi Moses
 */
public class GreetingRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection().registerType(GreetingMessage.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS);

        hints.reflection().registerType(WelcomeMessage.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS);
    }
}
