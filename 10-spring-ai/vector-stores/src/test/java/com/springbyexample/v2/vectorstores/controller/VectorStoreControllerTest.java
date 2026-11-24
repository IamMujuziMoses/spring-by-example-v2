package com.springbyexample.v2.vectorstores.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

import com.springbyexample.v2.vectorstores.dto.AddDocumentsResponse;
import com.springbyexample.v2.vectorstores.dto.VectorSearchResponse;
import com.springbyexample.v2.vectorstores.dto.VectorSearchResult;
import com.springbyexample.v2.vectorstores.service.VectorStoreService;

/**
 * Controller tests for vector store endpoints.
 *
 * @author Mujuzi Moses
 */
public class VectorStoreControllerTest {

    private MockMvc mockMvc;

    private VectorStoreService vectorStoreService;

    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        vectorStoreService = mock(VectorStoreService.class);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        VectorStoreController controller = new VectorStoreController(vectorStoreService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new VectorStoreExceptionHandler())
                .setValidator(validator).build();
    }

    @AfterEach
    void tearDown() {
        validator.destroy();
    }

    @Test
    void shouldAddDocuments() throws Exception {
        when(vectorStoreService.addDocuments(any())).thenReturn(new AddDocumentsResponse(1));

        String request = """
                {
                  "documents": [
                    {
                      "content": "Spring Boot simplifies Java development.",
                      "metadata": {
                        "category": "spring"
                      }
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/vector-stores/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isOk())
                .andExpect(jsonPath("$.documentsAdded").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenDocumentsAreMissing() throws Exception {
        mockMvc.perform(post("/api/vector-stores/documents").contentType(MediaType.APPLICATION_JSON)
                        .content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDocumentContentIsBlank() throws Exception {
        String request = """
                {
                  "documents": [
                    {
                      "content": " "
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/vector-stores/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchDocuments() throws Exception {
        when(vectorStoreService.search(any())).thenReturn(new VectorSearchResponse("Spring Boot",
                        List.of(new VectorSearchResult("document-1", "Spring Boot simplifies Java development.",
                                Map.of("category", "spring")))));

        String request = """
                {
                  "query": "Spring Boot",
                  "topK": 3
                }
                """;

        mockMvc.perform(post("/api/vector-stores/search").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Spring Boot"))
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[0].id").value("document-1"))
                .andExpect(jsonPath("$.results[0].content")
                        .value("Spring Boot simplifies Java development."));
    }

    @Test
    void shouldReturnBadRequestWhenSearchQueryIsBlank() throws Exception {
        String request = """
                {
                  "query": " ",
                  "topK": 5
                }
                """;

        mockMvc.perform(post("/api/vector-stores/search").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTopKIsOutsideAllowedRange() throws Exception {
        String request = """
                {
                  "query": "Spring Boot",
                  "topK": 25
                }
                """;

        mockMvc.perform(post("/api/vector-stores/search").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadGatewayWhenVectorStoreOperationFails() throws Exception {
        when(vectorStoreService.search(any())).thenThrow(new IllegalStateException("Search failed"));

        String request = """
                {
                  "query": "Spring Boot"
                }
                """;

        mockMvc.perform(post("/api/vector-stores/search").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isBadGateway()).andExpect(jsonPath("$.title")
                        .value("Vector Store Operation Failed"));
    }

    @Test
    void shouldReturnBadRequestWhenDocumentsListIsEmpty() throws Exception {
        String request = """
            {
              "documents": []
            }
            """;

        mockMvc.perform(post("/api/vector-stores/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenDocumentContentIsMissing() throws Exception {
        String request = """
            {
              "documents": [
                {
                  "metadata": {
                    "category": "spring"
                  }
                }
              ]
            }
            """;

        mockMvc.perform(post("/api/vector-stores/documents").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTopKIsBelowAllowedRange() throws Exception {
        String request = """
            {
              "query": "Spring Boot",
              "topK": 0
            }
            """;

        mockMvc.perform(post("/api/vector-stores/search").contentType(MediaType.APPLICATION_JSON)
                        .content(request)).andExpect(status().isBadRequest());
    }
}
