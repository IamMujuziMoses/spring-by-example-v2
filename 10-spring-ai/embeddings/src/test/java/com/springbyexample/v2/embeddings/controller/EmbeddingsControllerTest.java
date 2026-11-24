package com.springbyexample.v2.embeddings.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.springbyexample.v2.embeddings.dto.EmbeddedText;
import com.springbyexample.v2.embeddings.dto.EmbeddingResponse;
import com.springbyexample.v2.embeddings.service.EmbeddingsService;

import tools.jackson.databind.ObjectMapper;

/**
 * Tests for {@link EmbeddingsController}.
 *
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class EmbeddingsControllerTest {

    @Mock
    private EmbeddingsService embeddingsService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        EmbeddingsController controller = new EmbeddingsController(embeddingsService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new EmbeddingsExceptionHandler())
                .setValidator(validator).build();
    }

    @AfterEach
    void tearDown() {
        validator.destroy();
    }

    @Test
    void shouldGenerateEmbeddings() throws Exception {
        List<String> texts = List.of("Spring AI", "Spring Boot");

        EmbeddingResponse response = new EmbeddingResponse(List.of(
                new EmbeddedText("Spring AI", 3, List.of(0.1f, 0.2f, 0.3f)),
                new EmbeddedText("Spring Boot", 3, List.of(0.4f, 0.5f, 0.6f))
        ));

        when(embeddingsService.generateEmbeddings(texts)).thenReturn(response);

        mockMvc.perform(post("/api/embeddings").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("texts", texts))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embeddings.length()").value(2))
                .andExpect(jsonPath("$.embeddings[0].text").value("Spring AI"))
                .andExpect(jsonPath("$.embeddings[0].dimensions").value(3))
                .andExpect(jsonPath("$.embeddings[0].vector[0]").value(0.1));

        verify(embeddingsService).generateEmbeddings(texts);
    }

    @Test
    void shouldReturnBadRequestWhenTextsAreMissing() throws Exception {
        mockMvc.perform(post("/api/embeddings").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTextsAreEmpty() throws Exception {
        mockMvc.perform(post("/api/embeddings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "texts": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTextIsBlank() throws Exception {
        mockMvc.perform(post("/api/embeddings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "texts": ["   "]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTextContainsNullElement() throws Exception {
        mockMvc.perform(post("/api/embeddings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "texts": [null]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/api/embeddings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "texts":
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadGatewayWhenEmbeddingGenerationFails() throws Exception {
        doThrow(new IllegalStateException("Embedding model unavailable")).when(embeddingsService)
                .generateEmbeddings(anyList());

        mockMvc.perform(post("/api/embeddings").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "texts": ["Spring AI"]
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Embedding Generation Failed"))
                .andExpect(jsonPath("$.detail")
                        .value("The embedding model returned a response that could not be processed."));
    }
}
