package com.springbyexample.v2.retrievalaugmentedgeneration.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Retrieval-Augmented Generation example.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class RagConfiguration {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public ChatClient ragChatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        QuestionAnswerAdvisor questionAnswerAdvisor =
                QuestionAnswerAdvisor.builder(vectorStore).searchRequest(SearchRequest.builder().topK(4).build())
                        .build();

        return chatClientBuilder.defaultAdvisors(questionAnswerAdvisor).build();
    }
}
