package com.springbyexample.v2.embeddings.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.embeddings.dto.EmbeddingRequest;
import com.springbyexample.v2.embeddings.dto.EmbeddingResponse;
import com.springbyexample.v2.embeddings.service.EmbeddingsService;

import jakarta.validation.Valid;

/**
 * REST controller for generating text embeddings.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingsController {

    private final EmbeddingsService embeddingsService;

    public EmbeddingsController(EmbeddingsService embeddingsService) {
        this.embeddingsService = embeddingsService;
    }

    @PostMapping
    public ResponseEntity<EmbeddingResponse> generateEmbeddings(@Valid @RequestBody EmbeddingRequest request) {
        EmbeddingResponse response = embeddingsService.generateEmbeddings(request.texts());

        return ResponseEntity.ok(response);
    }
}
