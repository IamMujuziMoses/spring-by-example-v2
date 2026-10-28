package com.springbyexample.v2.springsecurity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests authentication and authorization through the application's HTTP layer.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SpringSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectUnauthenticatedRequestToUserProfile() throws Exception {
        mockMvc.perform(get("/api/user/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedRequestToAdminReports() throws Exception {
        mockMvc.perform(get("/api/admin/reports")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .with(httpBasic("reader", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldForbidReaderFromAccessingAdminReports() throws Exception {
        mockMvc.perform(get("/api/admin/reports")
                        .with(httpBasic("reader", "reader-password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToAccessUserProfile() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .with(httpBasic("admin", "admin-password")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyUnconfiguredEndpoints() throws Exception {
        mockMvc.perform(get("/api/not-configured")
                        .with(httpBasic("admin", "admin-password")))
                .andExpect(status().isForbidden());
    }
}