package com.springbyexample.v2.securitycontext;

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
public class SecurityContextApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk())
                .andExpect(content().string("This endpoint is public."));
    }

    @Test
    void authenticationEndpoint_shouldExposeCurrentAuthentication() throws Exception {
        mockMvc.perform(get("/authentication").with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("Authenticated as: user"));
    }

    @Test
    void securityContextEndpoint_shouldExposeAuthenticationFromSecurityContext() throws Exception {
        mockMvc.perform(get("/security-context").with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("Authenticated as: user"));
    }

    @Test
    void detailsEndpoint_shouldExposeAuthenticationDetails() throws Exception {
        mockMvc.perform(get("/details").with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("Name: user, Authorities: [FACTOR_PASSWORD, ROLE_USER]"));
    }

    @Test
    void authenticationEndpoint_shouldRejectUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/authentication")).andExpect(status().isUnauthorized());
    }

    @Test
    void securityContextEndpoint_shouldRejectUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/security-context")).andExpect(status().isUnauthorized());
    }

    @Test
    void detailsEndpoint_shouldRejectUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/details")).andExpect(status().isUnauthorized());
    }
}
