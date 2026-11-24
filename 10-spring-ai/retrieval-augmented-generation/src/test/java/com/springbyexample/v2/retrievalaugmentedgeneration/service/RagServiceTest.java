package com.springbyexample.v2.retrievalaugmentedgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeResponse;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionResponse;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.KnowledgeDocument;

/**
 * Unit tests for {@link RagService}.
 *
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class RagServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private RagService ragService;

    @Captor
    private ArgumentCaptor<List<Document>> documentsCaptor;

    @Test
    void shouldAddKnowledgeDocumentsToVectorStore() {
        var documents = List.of(
                new KnowledgeDocument("Spring Boot simplifies application development.",
                        Map.of("category", "spring")),
                new KnowledgeDocument("Spring AI provides abstractions for AI integration.",
                        Map.of("category", "ai"))
        );

        var request = new AddKnowledgeRequest(documents);

        AddKnowledgeResponse response = ragService.addKnowledge(request);

        assertThat(response.documentsAdded()).isEqualTo(2);

        verify(vectorStore).add(documentsCaptor.capture());

        assertThat(documentsCaptor.getValue()).hasSize(2);
    }

    @Test
    void shouldPreserveDocumentContentAndMetadata() {
        var metadata = Map.<String, Object>of("category", "spring", "source", "documentation");
        var request = new AddKnowledgeRequest(
                List.of(new KnowledgeDocument("Spring Boot simplifies application development.", metadata)));

        ragService.addKnowledge(request);

        verify(vectorStore).add(documentsCaptor.capture());

        Document document = documentsCaptor.getValue().getFirst();

        assertThat(document.getText()).isEqualTo("Spring Boot simplifies application development.");
        assertThat(document.getMetadata()).containsAllEntriesOf(metadata);
    }

    @Test
    void shouldUseEmptyMetadataWhenDocumentMetadataIsNull() {
        var request = new AddKnowledgeRequest(
                List.of(new KnowledgeDocument("Spring AI integrates AI capabilities into Spring applications.",
                                null)));

        ragService.addKnowledge(request);
        verify(vectorStore).add(documentsCaptor.capture());

        Document document = documentsCaptor.getValue().getFirst();

        assertThat(document.getMetadata()).isEmpty();
    }

    @Test
    void shouldReturnNumberOfDocumentsAdded() {
        var request = new AddKnowledgeRequest(
                List.of(
                        new KnowledgeDocument("Document one", Map.of()),
                        new KnowledgeDocument("Document two", Map.of()),
                        new KnowledgeDocument("Document three", Map.of())
                )
        );

        AddKnowledgeResponse response = ragService.addKnowledge(request);

        assertThat(response.documentsAdded()).isEqualTo(3);
    }

    @Test
    void shouldAnswerQuestionUsingChatClient() {
        String question = "What is Spring Boot?";
        String answer = "Spring Boot simplifies Spring application development.";

        mockChatClientResponse(answer);

        AskQuestionResponse response = ragService.askQuestion(new AskQuestionRequest(question));

        assertThat(response.question()).isEqualTo(question);
        assertThat(response.answer()).isEqualTo(answer);

        verify(chatClient).prompt();
        verify(requestSpec).user(question);
        verify(requestSpec).call();
        verify(responseSpec).content();
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsNullResponse() {
        mockChatClientResponse(null);

        assertThatThrownBy(() -> ragService.askQuestion(new AskQuestionRequest("What is Spring Boot?")))
                .isInstanceOf(IllegalStateException.class).hasMessage("The AI model returned an empty response.");
    }

    @Test
    void shouldThrowExceptionWhenChatModelReturnsBlankResponse() {
        mockChatClientResponse("   ");

        assertThatThrownBy(() -> ragService.askQuestion(new AskQuestionRequest("What is Spring Boot?")))
                .isInstanceOf(IllegalStateException.class).hasMessage("The AI model returned an empty response.");
    }

    @Test
    void shouldPropagateExceptionWhenAddingDocumentsFails() {
        var request = new AddKnowledgeRequest(
                List.of(new KnowledgeDocument("Spring Boot documentation", Map.of())));

        doThrow(new IllegalStateException("Vector store unavailable")).when(vectorStore).add(anyList());

        assertThatThrownBy(() -> ragService.addKnowledge(request))
                .isInstanceOf(IllegalStateException.class).hasMessage("Vector store unavailable");
    }

    @Test
    void shouldPropagateExceptionWhenGeneratingAnswerFails() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new IllegalStateException("Chat model unavailable"));

        assertThatThrownBy(() -> ragService.askQuestion(new AskQuestionRequest("What is Spring Boot?")))
                .isInstanceOf(IllegalStateException.class).hasMessage("Chat model unavailable");
    }

    private void mockChatClientResponse(String answer) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(answer);
    }
}
