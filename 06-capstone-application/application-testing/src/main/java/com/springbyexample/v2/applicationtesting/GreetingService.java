package com.springbyexample.v2.applicationtesting;

import org.springframework.stereotype.Service;

/**
 * Provides greeting messages.
 *
 * @author Mujuzi Moses
 */
@Service
public class GreetingService {

    public Greeting greet(String name) {
        return new Greeting("Hello, " + name + "!");
    }
}
