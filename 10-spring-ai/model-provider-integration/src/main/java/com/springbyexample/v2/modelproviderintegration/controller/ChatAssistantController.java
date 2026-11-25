package com.springbyexample.v2.modelproviderintegration.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.modelproviderintegration.dto.ChatRequest;
import com.springbyexample.v2.modelproviderintegration.dto.ChatResponse;
import com.springbyexample.v2.modelproviderintegration.service.ChatAssistantService;

import jakarta.validation.Valid;

/**
 * Exposes REST endpoints for the AI assistant.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/model-provider")
public class ChatAssistantController {

    private final ChatAssistantService chatAssistantService;

    public ChatAssistantController(ChatAssistantService chatAssistantService) {
        this.chatAssistantService = chatAssistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatAssistantService.answerQuestion(request.question());

        return ResponseEntity.ok(response);
    }
}
