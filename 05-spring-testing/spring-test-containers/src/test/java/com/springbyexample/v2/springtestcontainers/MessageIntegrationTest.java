package com.springbyexample.v2.springtestcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.springbyexample.v2.springtestcontainers.repository.MessageRepository;

/**
 * Integration tests that use a containerized PostgreSQL database.
 *
 * @author Mujuzi Moses
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class MessageIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("messages")
                    .withUsername("test")
                    .withPassword("test");

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
                                    "content": "Testcontainers integration testing"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.content")
                        .value("Testcontainers integration testing"));

        assertThat(messageRepository.findAll())
                .extracting(Message::getContent)
                .containsExactly("Testcontainers integration testing");
    }

    @Test
    void shouldRetrievePersistedMessage() throws Exception {
        Message savedMessage = messageRepository.save(new Message("Persisted PostgreSQL message"));

        mockMvc.perform(get("/messages/{id}", savedMessage.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMessage.getId()))
                .andExpect(jsonPath("$.content")
                        .value("Persisted PostgreSQL message"));
    }

    @Test
    void shouldReturnNotFoundForMissingMessage() throws Exception {
        mockMvc.perform(get("/messages/{id}", 999999L)).andExpect(status().isNotFound());
    }
}
