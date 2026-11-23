package com.springbyexample.v2.chatmodels.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.chatmodels.dto.ChatModelRequest;
import com.springbyexample.v2.chatmodels.dto.ChatModelResponse;
import com.springbyexample.v2.chatmodels.service.ChatModelService;

import jakarta.validation.Valid;

/**
 * Exposes REST endpoints for interacting with chat models.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/chat-models")
public class ChatModelController {

    private final ChatModelService chatModelService;

    public ChatModelController(ChatModelService chatModelService) {
        this.chatModelService = chatModelService;
    }

    /**
     * Generates a response using the configured chat model.
     *
     * @param request the incoming chat model request
     * @return the generated response
     */
    @PostMapping
    public ResponseEntity<ChatModelResponse> chat(@Valid @RequestBody ChatModelRequest request) {
        String response;

        if (request.temperature() == null) {
            response = chatModelService.chat(request.message());
        } else {
            response = chatModelService.chat(request.message(), request.temperature());
        }

        return ResponseEntity.ok(new ChatModelResponse(response));
    }
}
