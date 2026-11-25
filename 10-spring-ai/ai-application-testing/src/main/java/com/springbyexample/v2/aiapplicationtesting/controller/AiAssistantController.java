package com.springbyexample.v2.aiapplicationtesting.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.aiapplicationtesting.dto.AiAnswerResponse;
import com.springbyexample.v2.aiapplicationtesting.dto.AiQuestionRequest;
import com.springbyexample.v2.aiapplicationtesting.service.AiAssistantService;

import jakarta.validation.Valid;

/**
 * Exposes the AI assistant through a REST API.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/ai-assistant")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping
    public AiAnswerResponse answerQuestion(@Valid @RequestBody AiQuestionRequest request) {
        return aiAssistantService.answerQuestion(request.question());
    }
}
