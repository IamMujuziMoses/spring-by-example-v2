package com.springbyexample.v2.retrievalaugmentedgeneration.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeResponse;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionResponse;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.KnowledgeDocument;

/**
 * Service implementing Retrieval-Augmented Generation operations.
 *
 * @author Mujuzi Moses
 */
@Service
public class RagService {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

    public RagService(ChatClient ragChatClient, VectorStore vectorStore) {
        this.chatClient = ragChatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * Adds knowledge documents to the vector store.
     *
     * @param request the document ingestion request
     * @return the number of documents added
     */
    public AddKnowledgeResponse addKnowledge(AddKnowledgeRequest request) {
        List<Document> documents = request.documents().stream().map(this::toDocument).toList();
        vectorStore.add(documents);

        return new AddKnowledgeResponse(documents.size());
    }

    /**
     * Answers a question using context retrieved from the vector store.
     *
     * @param request the question request
     * @return the generated answer
     */
    public AskQuestionResponse askQuestion(AskQuestionRequest request) {
        String answer = chatClient.prompt().user(request.question()).call().content();

        if (answer == null || answer.isBlank()) {
            throw new IllegalStateException("The AI model returned an empty response.");
        }

        return new AskQuestionResponse(request.question(), answer);
    }

    private Document toDocument(KnowledgeDocument knowledgeDocument) {
        Map<String, Object> metadata = knowledgeDocument.metadata() == null ? Map.of() : knowledgeDocument.metadata();

        return new Document(knowledgeDocument.content(), metadata);
    }
}
