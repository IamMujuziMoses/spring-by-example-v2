package com.springbyexample.v2.basicspringsecurity;

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
public class BasicSecurityApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk());
    }

    @Test
    void privateEndpoint_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/private")).andExpect(status().isUnauthorized());
    }

    @Test
    void privateEndpoint_shouldBeAccessibleWithAuthentication() throws Exception {
        mockMvc.perform(get("/private").with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }
}
