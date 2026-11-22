package com.springbyexample.v2.springaifundamentals.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.springaifundamentals.dto.ChatRequest;
import com.springbyexample.v2.springaifundamentals.dto.ChatResponse;
import com.springbyexample.v2.springaifundamentals.service.AiChatService;

import jakarta.validation.Valid;

/**
 * Exposes REST endpoints for interacting with the AI chat service.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/ai/chat")
public class ChatController {

    private final AiChatService aiChatService;

    public ChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * Sends a message to the AI model.
     *
     * @param request the incoming chat request
     * @return the generated AI response
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String response = aiChatService.chat(request.message());

        return ResponseEntity.ok(new ChatResponse(response));
    }
}
