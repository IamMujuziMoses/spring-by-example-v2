package com.springbyexample.v2.userdetailsservice;

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
public class UserDetailsServiceApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk());
    }

    @Test
    void userEndpoint_shouldAuthenticateConfiguredUser() throws Exception {
        mockMvc.perform(get("/user").with(httpBasic("user", "password")))
                .andExpect(status().isOk()).andExpect(content().string("Authenticated user: user"));
    }

    @Test
    void userEndpoint_shouldRejectUnknownUser() throws Exception {
        mockMvc.perform(get("/user").with(httpBasic("unknown", "password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userEndpoint_shouldRejectInvalidPassword() throws Exception {
        mockMvc.perform(get("/user").with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }
}
