package com.springbyexample.v2.nativeimageconstraints.service;

import org.springframework.stereotype.Service;

/**
 * Demonstrates dynamic class loading in a native-image application.
 *
 * @author Mujuzi Moses
 */
@Service
public class ConstraintDemoService {

    private static final String MESSAGE_PACKAGE = "com.springbyexample.v2.nativeimageconstraints.message.";

    public String loadMessage(String type) {
        if (!"Greeting".equals(type) && !"Welcome".equals(type)) {
            throw new IllegalArgumentException("Unsupported message type: " + type);
        }

        String className = MESSAGE_PACKAGE + type + "Message";
        return loadMessageByClassName(className);
    }

    Object instantiateMessage(String className) throws ReflectiveOperationException {
        Class<?> messageClass = Class.forName(className);
        return messageClass.getDeclaredConstructor().newInstance();
    }

    String loadMessageByClassName(String className) {
        try {
            Object message = instantiateMessage(className);
            return (String) message.getClass().getMethod("getMessage").invoke(message);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to load message implementation: " + className, exception);
        }
    }
}
