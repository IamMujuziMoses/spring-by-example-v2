package com.springbyexample.v2.methodsecurity;

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
public class MethodSecurityApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void user_shouldAccessUserMethod() throws Exception {
        mockMvc.perform(get("/user").with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void user_shouldNotAccessAdminMethod() throws Exception {
        mockMvc.perform(get("/admin").with(httpBasic("user", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_shouldAccessAdminMethod() throws Exception {
        mockMvc.perform(get("/admin").with(httpBasic("admin", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUser_shouldAccessAuthenticatedMethod() throws Exception {
        mockMvc.perform(get("/authenticated").with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUser_shouldNotAccessProtectedMethod() throws Exception {
        mockMvc.perform(get("/authenticated")).andExpect(status().isUnauthorized());
    }
}
