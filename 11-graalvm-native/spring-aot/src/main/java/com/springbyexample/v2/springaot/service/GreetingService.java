package com.springbyexample.v2.springaot.service;

import org.springframework.stereotype.Service;

/**
 * Provides greeting messages for the Spring AOT example.
 *
 * @author Mujuzi Moses
 */
@Service
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "! Welcome to Spring AOT.";
    }
}
