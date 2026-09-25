package com.springbyexample.v2.securityfilterchain;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class SecurityFilterChainApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk());
    }

    @Test
    void userEndpoint_shouldRequireUserRole() throws Exception {
        mockMvc.perform(get("/user").with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_shouldRejectUserWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/admin").with(httpBasic("user", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldAllowAdminRole() throws Exception {
        mockMvc.perform(get("/admin").with(httpBasic("admin", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/authenticated")).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedEndpoint_shouldAllowAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/authenticated").with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void unknownEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/unknown")).andExpect(status().isUnauthorized());
    }

    @Test
    void unknownEndpoint_shouldBeDenied1() throws Exception {
        mockMvc.perform(get("/unknown").with(httpBasic("user", "password")))
                .andExpect(status().isForbidden());
    }
}
