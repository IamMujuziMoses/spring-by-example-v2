package com.springbyexample.v2.vectorstores.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.vectorstores.dto.AddDocumentsRequest;
import com.springbyexample.v2.vectorstores.dto.AddDocumentsResponse;
import com.springbyexample.v2.vectorstores.dto.VectorSearchRequest;
import com.springbyexample.v2.vectorstores.dto.VectorSearchResponse;
import com.springbyexample.v2.vectorstores.service.VectorStoreService;

import jakarta.validation.Valid;

/**
 * REST endpoints for document ingestion and semantic search.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/vector-stores")
public class VectorStoreController {

    private final VectorStoreService vectorStoreService;

    public VectorStoreController(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    /**
     * Adds documents to the vector store.
     *
     * @param request documents to store
     * @return ingestion result
     */
    @PostMapping("/documents")
    public ResponseEntity<AddDocumentsResponse> addDocuments(@Valid @RequestBody AddDocumentsRequest request) {
        AddDocumentsResponse response = vectorStoreService.addDocuments(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Searches for documents by semantic similarity.
     *
     * @param request search parameters
     * @return matching documents
     */
    @PostMapping("/search")
    public ResponseEntity<VectorSearchResponse> search(@Valid @RequestBody VectorSearchRequest request) {
        VectorSearchResponse response = vectorStoreService.search(request);

        return ResponseEntity.ok(response);
    }
}
