package com.springbyexample.v2.nativeexecutable.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.nativeexecutable.dto.GreetingResponse;

/**
 * REST controller demonstrating an endpoint in a native-image-ready application.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/native")
public class GreetingController {

    @GetMapping("/greeting")
    public GreetingResponse greeting() {
        return new GreetingResponse("Hello from Spring Boot!");
    }
}