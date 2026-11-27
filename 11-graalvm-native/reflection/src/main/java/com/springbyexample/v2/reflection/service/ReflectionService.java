package com.springbyexample.v2.reflection.service;

import org.springframework.stereotype.Service;

/**
 * Demonstrates reflective class loading and method invocation.
 *
 * @author Mujuzi Moses
 */
@Service
public class ReflectionService {

    private static final String MESSAGE_PACKAGE = "com.springbyexample.v2.reflection.message.";

    public String getMessage(String type) {
        if (!"Greeting".equals(type) && !"Welcome".equals(type)) {
            throw new IllegalArgumentException("Unsupported message type: " + type);
        }

        String className = MESSAGE_PACKAGE + type + "Message";

        return getMessageByClassName(className);
    }

    String getMessageByClassName(String className) {
        try {
            Object message = instantiateMessage(className);
            return (String) message.getClass().getMethod("getMessage").invoke(message);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Unable to invoke message implementation: " + className,
                    exception);
        }
    }

    Object instantiateMessage(String className) throws ReflectiveOperationException {
        Class<?> messageClass = Class.forName(className);
        return messageClass.getDeclaredConstructor().newInstance();
    }
}
