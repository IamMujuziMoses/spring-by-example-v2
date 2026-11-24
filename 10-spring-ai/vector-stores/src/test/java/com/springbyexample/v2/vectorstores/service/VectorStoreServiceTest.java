package com.springbyexample.v2.vectorstores.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import com.springbyexample.v2.vectorstores.dto.AddDocumentsRequest;
import com.springbyexample.v2.vectorstores.dto.AddDocumentsResponse;
import com.springbyexample.v2.vectorstores.dto.DocumentInput;
import com.springbyexample.v2.vectorstores.dto.VectorSearchRequest;
import com.springbyexample.v2.vectorstores.dto.VectorSearchResponse;

/**
 * Unit tests for the vector store service.
 *
 * @author Mujuzi Moses
 */
public class VectorStoreServiceTest {

    private VectorStore vectorStore;

    private VectorStoreService vectorStoreService;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        vectorStoreService = new VectorStoreService(vectorStore);
    }

    @Test
    void shouldAddDocumentsToVectorStore() {
        Map<String, Object> metadata = Map.of("category", "spring", "source", "documentation");

        AddDocumentsRequest request = new AddDocumentsRequest(List.of(
                new DocumentInput("Spring Boot simplifies Java development.", metadata),
                new DocumentInput("Spring AI integrates AI capabilities.", Map.of())
        ));

        AddDocumentsResponse response = vectorStoreService.addDocuments(request);

        assertThat(response.documentsAdded()).isEqualTo(2);
        verify(vectorStore).add(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPreserveDocumentContentAndMetadata() {
        Map<String, Object> metadata = Map.of("category", "spring");

        AddDocumentsRequest request = new AddDocumentsRequest(List.of(
                new DocumentInput("Spring Boot documentation.", metadata)));

        vectorStoreService.addDocuments(request);

        var documentCaptor = forClass(List.class);

        verify(vectorStore).add(documentCaptor.capture());

        List<Document> documents = (List<Document>) documentCaptor.getValue();

        assertThat(documents).hasSize(1);
        assertThat(documents.getFirst().getText()).isEqualTo("Spring Boot documentation.");
        assertThat(documents.getFirst().getMetadata()).containsEntry("category", "spring");
    }

    @Test
    void shouldSearchDocumentsUsingDefaultResultLimit() {
        VectorSearchRequest request = new VectorSearchRequest("Spring Boot", null);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("Spring Boot simplifies Java development.")));

        VectorSearchResponse response = vectorStoreService.search(request);

        assertThat(response.query()).isEqualTo("Spring Boot");
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().getFirst().content()).isEqualTo("Spring Boot simplifies Java development.");

        var searchRequestCaptor = forClass(SearchRequest.class);

        verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
        assertThat(searchRequestCaptor.getValue().getQuery()).isEqualTo("Spring Boot");
        assertThat(searchRequestCaptor.getValue().getTopK()).isEqualTo(5);
    }

    @Test
    void shouldSearchDocumentsUsingCustomResultLimit() {
        VectorSearchRequest request = new VectorSearchRequest("Java development", 3);

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        vectorStoreService.search(request);

        var searchRequestCaptor = forClass(SearchRequest.class);

        verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
        assertThat(searchRequestCaptor.getValue().getTopK()).isEqualTo(3);
    }

    @Test
    void shouldReturnEmptyResultsWhenNoDocumentsMatch() {
        VectorSearchRequest request = new VectorSearchRequest("Unknown technology", 5);

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        VectorSearchResponse response = vectorStoreService.search(request);

        assertThat(response.results()).isEmpty();
    }

    @Test
    void shouldPropagateVectorStoreExceptionWhenAddingDocumentsFails() {
        AddDocumentsRequest request = new AddDocumentsRequest(List.of(
                new DocumentInput("Spring AI documentation.", Map.of())));

        org.mockito.Mockito.doThrow(new IllegalStateException("Storage failed")).when(vectorStore).add(anyList());

        assertThatThrownBy(() -> vectorStoreService.addDocuments(request)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Storage failed");
    }

    @Test
    void shouldPropagateVectorStoreExceptionWhenSearchFails() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenThrow(new IllegalStateException("Search failed"));

        assertThatThrownBy(() -> vectorStoreService.search(new VectorSearchRequest("Spring AI", 5)))
                .isInstanceOf(IllegalStateException.class).hasMessage("Search failed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUseEmptyMetadataWhenMetadataIsNull() {
        AddDocumentsRequest request = new AddDocumentsRequest(List.of(
                new DocumentInput("Spring AI documentation.", null)));

        vectorStoreService.addDocuments(request);

        var documentCaptor = org.mockito.ArgumentCaptor.forClass(List.class);

        verify(vectorStore).add(documentCaptor.capture());

        List<Document> documents = (List<Document>) documentCaptor.getValue();

        assertThat(documents).hasSize(1);
        assertThat(documents.getFirst().getMetadata()).isEmpty();
    }

    @Test
    void shouldMapSearchResultsWithMetadata() {
        Map<String, Object> metadata = Map.of("category", "spring", "source", "documentation");

        Document document = new Document("Spring AI integrates AI capabilities into Spring applications.", metadata);

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(document));

        VectorSearchResponse response = vectorStoreService.search(new VectorSearchRequest("Spring AI", 5));

        assertThat(response.results()).hasSize(1);
        assertThat(response.results().getFirst().content()).isEqualTo(document.getText());
        assertThat(response.results().getFirst().metadata()).containsEntry("category", "spring")
                .containsEntry("source", "documentation");
    }
}
