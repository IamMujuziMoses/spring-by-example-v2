package com.springbyexample.v2.vectorstores.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * Integration tests for document storage and semantic search.
 *
 * @author Mujuzi Moses
 */
public class VectorStoreIntegrationTest {

    @Test
    void shouldStoreAndRetrieveDocumentsUsingSemanticSimilarity() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);

        when(embeddingModel.embed(any(Document.class))).thenAnswer(invocation -> {
                    Document document = invocation.getArgument(0);

                    assertNotNull(document.getText());
                    return createEmbedding(document.getText());
                });

        when(embeddingModel.embed(anyString())).thenAnswer(invocation ->
                        createEmbedding(invocation.getArgument(0)));

        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        List<Document> documents = List.of(
                new Document("Spring Boot simplifies Java application development.",
                        Map.of("category", "spring")),
                new Document("Spring AI integrates AI capabilities into Spring applications.",
                        Map.of("category", "spring")),
                new Document("Cooking recipes require ingredients and preparation.",
                        Map.of("category", "cooking"))
        );

        vectorStore.add(documents);

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder().query("How do I build applications with Spring?").topK(2).build());

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Document::getText)
                .contains(
                        "Spring Boot simplifies Java application development.",
                        "Spring AI integrates AI capabilities into Spring applications."
                );

        assertThat(results).extracting(Document::getMetadata).allSatisfy(metadata ->
                        assertThat(metadata).containsEntry("category", "spring"));
    }

    private float[] createEmbedding(String text) {
        if (text.startsWith("Spring Boot")) {
            return new float[]{1.0F, 0.0F, 0.0F};
        }

        if (text.startsWith("Spring AI")) {
            return new float[]{0.9F, 0.1F, 0.0F};
        }

        if (text.startsWith("Cooking")) {
            return new float[]{0.0F, 1.0F, 0.0F};
        }

        if (text.startsWith("How do I build")) {
            return new float[]{0.95F, 0.05F, 0.0F};
        }

        throw new IllegalArgumentException("Unexpected text: " + text);
    }
}
