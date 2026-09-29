package com.springbyexample.v2.securitytesting;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class SecurityTestingApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk())
                .andExpect(content().string("This endpoint is public."));
    }

    @Test
    void protectedEndpointWithoutAuthentication_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/user")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithMockUser_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/user").with(user("user").roles("USER"))).andExpect(status().isOk())
                .andExpect(content().string("Authenticated as: user"));
    }

    @Test
    void protectedEndpointWithAnonymousUser_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/user").with(anonymous())).andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpointWithUserRole_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpointWithAdminRole_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string("Admin endpoint accessed."));
    }

    @Test
    void transferWithoutCsrfToken_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/transfer").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void transferWithCsrfToken_shouldBeAllowed() throws Exception {
        mockMvc.perform(post("/transfer").with(user("user").roles("USER")).with(csrf()))
                .andExpect(status().isOk()).andExpect(content().string("Transfer completed."));
    }

    @Test
    void profileWithMockUser_shouldExposeAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/profile").with(user("moses").roles("USER"))).andExpect(status().isOk())
                .andExpect(content().string("Profile: moses"));
    }

    @Test
    void userOperationWithUserRole_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/user-operation").with(user("user").roles("USER")))
                .andExpect(status().isOk()).andExpect(content().string("User operation executed."));
    }

    @Test
    void adminOperationWithUserRole_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin-operation").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOperationWithAdminRole_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/admin-operation").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string("Admin operation executed."));
    }

    @Test
    void authenticatedOperationWithMockUser_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/authenticated-operation").with(user("user").roles("USER")))
                .andExpect(status().isOk()).andExpect(content().string("Authenticated operation executed."));
    }

    @Test
    void userEndpointWithJwt_shouldBeAuthenticated() throws Exception {
        mockMvc.perform(get("/user").with(jwt())).andExpect(status().isOk());
    }

    @Test
    void userEndpointWithOAuth2Login_shouldBeAuthenticated() throws Exception {
        mockMvc.perform(get("/user").with(oauth2Login())).andExpect(status().isOk());
    }
}
