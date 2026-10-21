package com.springbyexample.v2.springmockmvc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class GreetingController {

    @GetMapping("/greetings")
    public String greet(@RequestParam(defaultValue = "Spring") String name) {
        return "Hello, " + name + "!";
    }
}
