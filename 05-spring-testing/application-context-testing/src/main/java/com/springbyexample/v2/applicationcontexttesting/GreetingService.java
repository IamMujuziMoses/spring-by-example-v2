package com.springbyexample.v2.applicationcontexttesting;

/**
 * Provides greeting messages.
 *
 * @author Mujuzi Moses
 */
public class GreetingService {

    /**
     * Creates a greeting for the specified name.
     *
     * @param name the name of the person to greet
     * @return the greeting message
     */
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
