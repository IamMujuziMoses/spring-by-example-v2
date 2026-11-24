package com.springbyexample.v2.embeddings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * Unit tests for {@link EmbeddingsService}.
 *
 * @author Mujuzi Moses
 */
public class EmbeddingsServiceTest {

    private EmbeddingModel embeddingModel;

    private EmbeddingsService embeddingsService;

    @BeforeEach
    void setUp() {
        embeddingModel = mock(EmbeddingModel.class);
        embeddingsService = new EmbeddingsService(embeddingModel);
    }

    @Test
    void shouldGenerateEmbeddingsForMultipleTexts() {
        List<String> texts = List.of(
                "Spring Boot simplifies Java development.",
                "Spring AI integrates AI capabilities into Spring applications."
        );

        when(embeddingModel.embed(texts)).thenReturn(
                List.of(new float[]{0.1f, 0.2f, 0.3f}, new float[]{0.4f, 0.5f, 0.6f}));

        var response = embeddingsService.generateEmbeddings(texts);

        assertThat(response.embeddings()).hasSize(2);
        assertThat(response.embeddings().getFirst().text()).isEqualTo(texts.getFirst());
        assertThat(response.embeddings().get(0).dimensions()).isEqualTo(3);
        assertThat(response.embeddings().get(0).vector()).containsExactly(0.1f, 0.2f, 0.3f);
        assertThat(response.embeddings().get(1).text()).isEqualTo(texts.get(1));
        assertThat(response.embeddings().get(1).dimensions()).isEqualTo(3);
        assertThat(response.embeddings().get(1).vector()).containsExactly(0.4f, 0.5f, 0.6f);

        verify(embeddingModel).embed(texts);
    }

    @Test
    void shouldThrowExceptionWhenEmbeddingResponseIsNull() {
        when(embeddingModel.embed(anyList())).thenReturn(null);

        assertThatThrownBy(() -> embeddingsService.generateEmbeddings(List.of("Spring AI")))
                .isInstanceOf(IllegalStateException.class).hasMessage("The embedding model returned an invalid response.");
    }

    @Test
    void shouldThrowExceptionWhenEmbeddingCountDoesNotMatchInputCount() {
        when(embeddingModel.embed(anyList())).thenReturn(List.of(new float[]{0.1f, 0.2f}));

        assertThatThrownBy(() -> embeddingsService.generateEmbeddings(
                        List.of("Spring AI", "Spring Boot")))
                .isInstanceOf(IllegalStateException.class).hasMessage("The embedding model returned an invalid response.");
    }

    @Test
    void shouldThrowExceptionWhenEmbeddingVectorIsNull() {
        when(embeddingModel.embed(anyList())).thenReturn(java.util.Collections.singletonList(null));

        assertThatThrownBy(() -> embeddingsService.generateEmbeddings(List.of("Spring AI")))
                .isInstanceOf(IllegalStateException.class).hasMessage("The embedding model returned an empty embedding.");
    }

    @Test
    void shouldThrowExceptionWhenEmbeddingVectorIsEmpty() {
        when(embeddingModel.embed(anyList())).thenReturn(List.of(new float[0]));

        assertThatThrownBy(() -> embeddingsService.generateEmbeddings(List.of("Spring AI")))
                .isInstanceOf(IllegalStateException.class).hasMessage("The embedding model returned an empty embedding.");
    }

    @Test
    void shouldPropagateEmbeddingModelException() {
        when(embeddingModel.embed(anyList())).thenThrow(new IllegalStateException("Embedding model unavailable"));

        assertThatThrownBy(() -> embeddingsService.generateEmbeddings(List.of("Spring AI")))
                .isInstanceOf(IllegalStateException.class).hasMessage("Embedding model unavailable");
    }
}
