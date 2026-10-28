package com.springbyexample.v2.springsecurity.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests the responses returned by the security demonstration endpoints.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnPublicResourceWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/public")).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("This resource is publicly accessible."));
    }

    @Test
    void shouldReturnUserProfileForReader() throws Exception {
        mockMvc.perform(get("/api/user/profile").with(httpBasic("reader", "reader-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value("USER"))
                .andExpect(jsonPath("$.message").value("You have accessed the user profile."));
    }

    @Test
    void shouldReturnAdminReportsForAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/reports").with(httpBasic("admin", "admin-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value("ADMIN"))
                .andExpect(jsonPath("$.message")
                        .value("You have accessed the administrator reports."));
    }
}
