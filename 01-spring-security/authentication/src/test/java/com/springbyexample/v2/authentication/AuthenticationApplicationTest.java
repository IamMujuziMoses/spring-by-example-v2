package com.springbyexample.v2.authentication;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk());
    }

    @Test
    void authenticatedEndpoint_shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/authenticated")).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedEndpoint_shouldAuthenticateValidCredentials() throws Exception {
        mockMvc.perform(get("/authenticated").with(httpBasic("user", "password")))
                .andExpect(status().isOk()).andExpect(content().string("Authenticated as: user"));
    }

    @Test
    void authenticatedEndpoint_shouldRejectInvalidPassword() throws Exception {
        mockMvc.perform(get("/authenticated").with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedEndpoint_shouldRejectUnknownUser() throws Exception {
        mockMvc.perform(get("/authenticated").with(httpBasic("unknown", "password")))
                .andExpect(status().isUnauthorized());
    }
}