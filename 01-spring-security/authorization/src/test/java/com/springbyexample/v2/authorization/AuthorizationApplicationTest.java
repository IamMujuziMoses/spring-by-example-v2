package com.springbyexample.v2.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizationApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk())
                .andExpect(content().string("This endpoint is public."));
    }

    @Test
    void authenticatedEndpoint_shouldBeAccessibleToAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/authenticated").with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("This endpoint requires authentication."));
    }

    @Test
    void authenticatedEndpoint_shouldRejectUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/authenticated")).andExpect(status().isUnauthorized());
    }

    @Test
    void userEndpoint_shouldBeAccessibleToUserRole() throws Exception {
        mockMvc.perform(get("/user").with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("This endpoint requires the USER role."));
    }

    @Test
    void adminEndpoint_shouldRejectUserRole() throws Exception {
        mockMvc.perform(get("/admin").with(httpBasic("user", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldBeAccessibleToAdminRole() throws Exception {
        mockMvc.perform(get("/admin").with(httpBasic("admin", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("This endpoint requires the ADMIN role."));
    }

    @Test
    void unknownEndpoint_shouldBeForbiddenForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/unknown").with(httpBasic("user", "password")))
                .andExpect(status().isForbidden());
    }
}
