package com.springbyexample.v2.prompttemplates.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.prompttemplates.dto.PromptTemplateRequest;
import com.springbyexample.v2.prompttemplates.dto.PromptTemplateResponse;
import com.springbyexample.v2.prompttemplates.service.PromptTemplateService;

import jakarta.validation.Valid;

/**
 * Exposes prompt template functionality through a REST API.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/prompt-templates")
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    public PromptTemplateController(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    @PostMapping
    public ResponseEntity<PromptTemplateResponse> generate(@Valid @RequestBody PromptTemplateRequest request) {

        String response = promptTemplateService.generateExplanation(request.technology(), request.audience(),
                request.detailLevel(), request.question());

        return ResponseEntity.ok(new PromptTemplateResponse(response));
    }
}
