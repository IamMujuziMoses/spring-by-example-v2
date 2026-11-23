package com.springbyexample.v2.structuredoutput.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.structuredoutput.dto.TechnologyRequest;
import com.springbyexample.v2.structuredoutput.model.TechnologyRecommendation;
import com.springbyexample.v2.structuredoutput.service.StructuredOutputService;

import jakarta.validation.Valid;

/**
 * REST controller for generating structured AI responses.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/structured-output")
public class StructuredOutputController {

    private final StructuredOutputService structuredOutputService;

    public StructuredOutputController(StructuredOutputService structuredOutputService) {
        this.structuredOutputService = structuredOutputService;
    }

    @PostMapping
    public TechnologyRecommendation generate(@Valid @RequestBody TechnologyRequest request) {

        return structuredOutputService.generateRecommendation(request.technology(), request.useCase(), request.audience());
    }
}
