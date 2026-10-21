package com.springbyexample.v2.springsecuritytesting;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
public class SpringSecurityTestingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowUnauthenticatedAccessToPublicEndpoint() throws Exception {
        mockMvc.perform(get("/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, everyone!"));
    }

    @Test
    void shouldRejectUnauthenticatedAccessToProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/greetings")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedUserAccessToProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/greetings").with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, authenticated user!"));
    }

    @Test
    void shouldRejectUserWithoutRequiredRole() throws Exception {
        mockMvc.perform(get("/admin").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdministratorAccessToAdminEndpoint() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").roles("USER", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, administrator!"));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        mockMvc.perform(get("/greetings").with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }
}
