package com.springbyexample.v2.springintegrationtesting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.springbyexample.v2.springintegrationtesting.repository.MessageRepository;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
class MessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
    }

    @Test
    void shouldCreateMessageAndPersistIt() throws Exception {
        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "content": "Integration testing"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content").value("Integration testing"));

        assertThat(messageRepository.findAll())
                .extracting(Message::getContent)
                .containsExactly("Integration testing");
    }

    @Test
    void shouldRetrievePersistedMessage() throws Exception {
        Message savedMessage = messageRepository.save(new Message("Persisted message"));

        MvcResult result = mockMvc.perform(get("/messages/{id}", savedMessage.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMessage.getId()))
                .andExpect(jsonPath("$.content").value("Persisted message"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("Persisted message");
    }

    @Test
    void shouldReturnNotFoundForMissingMessage() throws Exception {
        mockMvc.perform(get("/messages/{id}", 999999L)).andExpect(status().isNotFound());
    }
}
