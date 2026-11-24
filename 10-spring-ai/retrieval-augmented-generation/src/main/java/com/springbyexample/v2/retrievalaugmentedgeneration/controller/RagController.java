package com.springbyexample.v2.retrievalaugmentedgeneration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeResponse;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionResponse;
import com.springbyexample.v2.retrievalaugmentedgeneration.service.RagService;

import jakarta.validation.Valid;

/**
 * REST controller exposing Retrieval-Augmented Generation operations.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * Adds knowledge documents to the vector store.
     *
     * @param request the document ingestion request
     * @return the ingestion result
     */
    @PostMapping("/documents")
    public ResponseEntity<AddKnowledgeResponse> addKnowledge(@Valid @RequestBody AddKnowledgeRequest request) {
        AddKnowledgeResponse response = ragService.addKnowledge(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Answers a question using retrieved knowledge.
     *
     * @param request the question request
     * @return the generated answer
     */
    @PostMapping("/questions")
    public ResponseEntity<AskQuestionResponse> askQuestion(@Valid @RequestBody AskQuestionRequest request) {
        AskQuestionResponse response = ragService.askQuestion(request);

        return ResponseEntity.ok(response);
    }
}
