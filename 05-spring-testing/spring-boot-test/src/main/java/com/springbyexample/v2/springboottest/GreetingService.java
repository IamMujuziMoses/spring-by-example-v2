package com.springbyexample.v2.springboottest;

import org.springframework.stereotype.Service;

/**
 * @author Mujuzi Moses
 */
@Service
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
