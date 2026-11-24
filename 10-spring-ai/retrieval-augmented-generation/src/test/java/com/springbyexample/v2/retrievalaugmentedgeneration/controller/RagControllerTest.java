package com.springbyexample.v2.retrievalaugmentedgeneration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AddKnowledgeResponse;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionRequest;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.AskQuestionResponse;
import com.springbyexample.v2.retrievalaugmentedgeneration.dto.KnowledgeDocument;
import com.springbyexample.v2.retrievalaugmentedgeneration.service.RagService;

import tools.jackson.databind.ObjectMapper;

/**
 * Controller tests for {@link RagController}.
 *
 * @author Mujuzi Moses
 */
public class RagControllerTest {

    private RagService ragService;

    private MockMvc mockMvc;

    private LocalValidatorFactoryBean validator;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ragService = mock(RagService.class);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(new RagController(ragService))
                .setControllerAdvice(new RagExceptionHandler()).setValidator(validator).build();
    }

    @AfterEach
    void tearDown() {
        validator.destroy();
    }

    @Test
    void shouldAddKnowledgeDocuments() throws Exception {
        var request = new AddKnowledgeRequest(
                List.of(new KnowledgeDocument("Spring Boot simplifies application development.",
                        Map.of("category", "spring"))));

        when(ragService.addKnowledge(any(AddKnowledgeRequest.class))).thenReturn(new AddKnowledgeResponse(1));

        mockMvc.perform(post("/api/rag/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentsAdded").value(1));

        verify(ragService).addKnowledge(any(AddKnowledgeRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenDocumentsAreMissing() throws Exception {
        mockMvc.perform(post("/api/rag/documents").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDocumentsListIsEmpty() throws Exception {
        mockMvc.perform(post("/api/rag/documents").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "documents": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDocumentContentIsBlank() throws Exception {
        mockMvc.perform(post("/api/rag/documents").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "documents": [
                                        {
                                            "content": "   "
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDocumentContentIsMissing() throws Exception {
        mockMvc.perform(post("/api/rag/documents").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "documents": [
                                        {
                                            "metadata": {
                                                "category": "spring"
                                            }
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAnswerQuestion() throws Exception {
        String question = "What is Spring Boot?";
        String answer = "Spring Boot simplifies Spring application development.";

        when(ragService.askQuestion(any(AskQuestionRequest.class))).thenReturn(new AskQuestionResponse(question, answer));

        mockMvc.perform(post("/api/rag/questions").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AskQuestionRequest(question))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value(question))
                .andExpect(jsonPath("$.answer").value(answer));

        verify(ragService).askQuestion(any(AskQuestionRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenQuestionIsMissing() throws Exception {
        mockMvc.perform(post("/api/rag/questions").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenQuestionIsBlank() throws Exception {
        mockMvc.perform(post("/api/rag/questions").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "question": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadGatewayWhenDocumentIngestionFails() throws Exception {
        doThrow(new IllegalStateException("Vector store unavailable")).when(ragService)
                .addKnowledge(any(AddKnowledgeRequest.class));

        mockMvc.perform(post("/api/rag/documents").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "documents": [
                                        {
                                            "content": "Spring Boot documentation"
                                        }
                                    ]
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("RAG Operation Failed"))
                .andExpect(jsonPath("$.detail")
                        .value("The AI service could not complete the requested operation."));
    }

    @Test
    void shouldReturnBadGatewayWhenQuestionAnsweringFails() throws Exception {
        doThrow(new IllegalStateException("Chat model unavailable")).when(ragService)
                .askQuestion(any(AskQuestionRequest.class));

        mockMvc.perform(post("/api/rag/questions").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "question": "What is Spring Boot?"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("RAG Operation Failed"));
    }
}
