package com.springbyexample.v2.retrievalaugmentedgeneration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Content;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;

import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.KnowledgeDocument;

/**
 * Integration tests for the Retrieval-Augmented Generation workflow.
 *
 * @author Mujuzi Moses
 */
public class RagIntegrationTest {

    private ChatModel chatModel;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        chatModel = mock(ChatModel.class);
        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());

        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(1).build()).build();

        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(advisor).build();

        ragService = new RagService(chatClient, vectorStore);

        ChatResponse response = ChatResponse.builder()
                .generations(List.of(new Generation(
                        new AssistantMessage("Spring Boot uses auto-configuration to simplify application setup."
                        ))
                ))
                .metadata(ChatResponseMetadata.builder().model("test-model").build())
                .build();

        when(chatModel.call(any(Prompt.class))).thenReturn(response);

        when(embeddingModel.embed(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);

            assertNotNull(document.getText());
            return createEmbedding(document.getText());
        });

        when(embeddingModel.embed(anyString()))
                .thenAnswer(invocation -> createEmbedding(invocation.getArgument(0)));
    }

    @Test
    void shouldRetrieveRelevantDocumentsAndGenerateAnswer() {
        var documents = List.of(
                new KnowledgeDocument("Spring Boot uses auto-configuration to simplify application setup.",
                        Map.of("category", "spring-boot")),
                new KnowledgeDocument("Spring Security provides authentication and authorization.",
                        Map.of("category", "spring-security"))
        );

        ragService.addKnowledge(new AddKnowledgeRequest(documents));

        String question = "How does Spring Boot configure applications?";

        var response = ragService.askQuestion(new AskQuestionRequest(question));

        assertThat(response.question()).isEqualTo(question);
        assertThat(response.answer()).contains("auto-configuration");

        var promptCaptor = org.mockito.ArgumentCaptor.forClass(Prompt.class);

        verify(chatModel).call(promptCaptor.capture());

        String promptContent = promptCaptor.getValue().getInstructions().stream().map(Content::getText)
                .reduce("", (first, second) -> first + "\n" + second);

        assertThat(promptContent).contains("Spring Boot uses auto-configuration to simplify application setup.");
        assertThat(promptContent).doesNotContain("Spring Security provides authentication and authorization.");
    }

    private float[] createEmbedding(String text) {
        if (text.contains("Spring Boot") || text.contains("configure applications")) {
            return new float[]{1.0F, 0.0F};
        }

        if (text.contains("Spring Security")) {
            return new float[]{0.0F, 1.0F};
        }

        throw new IllegalArgumentException("Unexpected text supplied for embedding: " + text);
    }
}
