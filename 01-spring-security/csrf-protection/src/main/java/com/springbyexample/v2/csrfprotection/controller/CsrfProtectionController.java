package com.springbyexample.v2.csrfprotection.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mujuzi Moses
 */
@RestController
public class CsrfProtectionController {

    @PostMapping("/transfer")
    public String transfer() {
        return "Transfer completed.";
    }
}
