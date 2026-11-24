package com.springbyexample.v2.retrievalaugmentedgeneration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Application context tests for the RAG example.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class RagApplicationTest {

    @Autowired
    private ChatClient ragChatClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Test
    void shouldLoadApplicationContext() {
        assertThat(ragChatClient).isNotNull();
        assertThat(vectorStore).isNotNull();
        assertThat(embeddingModel).isNotNull();
    }
}
