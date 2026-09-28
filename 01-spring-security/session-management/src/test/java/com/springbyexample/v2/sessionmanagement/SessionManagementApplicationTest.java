package com.springbyexample.v2.sessionmanagement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.HttpSession;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SessionManagementApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointWithoutAuthentication_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/session")).andExpect(status().is3xxRedirection());
    }

    @Test
    void authenticatedRequest_shouldHaveAccessToSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/session").with(user("user").roles("USER")))
                .andExpect(status().isOk()).andReturn();

        HttpSession session = result.getRequest().getSession(false);

        assertNotNull(session);
    }

    @Test
    void authenticatedRequests_shouldUseSameSession() throws Exception {
        // Start with an authenticated request and capture its session.
        MvcResult firstRequest = mockMvc.perform(get("/session").with(user("user").roles("USER")))
                .andExpect(status().isOk()).andReturn();

        HttpSession session = firstRequest.getRequest().getSession(false);

        assertNotNull(session);

        // Reuse the session for the next authenticated request.
        MvcResult secondRequest = mockMvc.perform(get("/session")
                .session((org.springframework.mock.web.MockHttpSession) session)
                .with(user("user").roles("USER"))).andExpect(status().isOk()).andReturn();

        // Both requests should use the same session.
        assert session.getId().equals(Objects.requireNonNull(secondRequest.getRequest().getSession(false)).getId());
    }
}
