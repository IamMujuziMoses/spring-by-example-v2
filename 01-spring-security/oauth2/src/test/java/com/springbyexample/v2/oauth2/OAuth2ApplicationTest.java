package com.springbyexample.v2.oauth2;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
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
public class OAuth2ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void home_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string("OAuth2 Login Example"));
    }

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk())
                .andExpect(content().string("This endpoint is public."));
    }

    @Test
    void userEndpointWithoutAuthentication_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/user")).andExpect(status().is3xxRedirection());
    }

    @Test
    void userEndpoint_withOAuth2Login_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/user").with(oauth2Login())).andExpect(status().isOk());
    }
}
