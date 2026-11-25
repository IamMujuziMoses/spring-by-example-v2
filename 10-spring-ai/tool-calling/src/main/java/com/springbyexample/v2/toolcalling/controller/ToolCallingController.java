package com.springbyexample.v2.toolcalling.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.toolcalling.dto.ToolCallingRequest;
import com.springbyexample.v2.toolcalling.dto.ToolCallingResponse;
import com.springbyexample.v2.toolcalling.service.ToolCallingService;

import jakarta.validation.Valid;

/**
 * Exposes REST endpoints for AI tool calling operations.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/tool-calling")
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    public ToolCallingController(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    @PostMapping
    public ResponseEntity<ToolCallingResponse> answerQuestion(@Valid @RequestBody ToolCallingRequest request) {
        ToolCallingResponse response = toolCallingService.answerQuestion(request.question());

        return ResponseEntity.ok(response);
    }
}
