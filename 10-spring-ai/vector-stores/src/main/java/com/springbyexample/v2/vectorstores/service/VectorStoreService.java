package com.springbyexample.v2.vectorstores.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.vectorstores.dto.AddDocumentsRequest;
import com.springbyexample.v2.vectorstores.dto.AddDocumentsResponse;
import com.springbyexample.v2.vectorstores.dto.VectorSearchRequest;
import com.springbyexample.v2.vectorstores.dto.VectorSearchResponse;
import com.springbyexample.v2.vectorstores.dto.VectorSearchResult;

/**
 * Provides document ingestion and semantic search operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class VectorStoreService {

    private static final int DEFAULT_TOP_K = 5;

    private final VectorStore vectorStore;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Adds documents to the vector store.
     *
     * @param request documents to add
     * @return number of documents added
     */
    public AddDocumentsResponse addDocuments(AddDocumentsRequest request) {
        List<Document> documents = request.documents().stream()
                .map(input -> new Document(input.content(),
                        input.metadata() == null ? Map.of() : input.metadata())).toList();

        vectorStore.add(documents);

        return new AddDocumentsResponse(documents.size());
    }

    /**
     * Searches for documents using semantic similarity.
     *
     * @param request search parameters
     * @return matching documents
     */
    public VectorSearchResponse search(VectorSearchRequest request) {
        int topK = request.topK() == null ? DEFAULT_TOP_K : request.topK();
        SearchRequest searchRequest = SearchRequest.builder().query(request.query()).topK(topK).build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        List<VectorSearchResult> results = documents.stream().map(document -> new VectorSearchResult(
                        document.getId(), document.getText(), document.getMetadata())).toList();

        return new VectorSearchResponse(request.query(), results);
    }

}
