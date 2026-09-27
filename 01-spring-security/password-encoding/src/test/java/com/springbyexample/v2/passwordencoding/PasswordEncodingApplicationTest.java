package com.springbyexample.v2.passwordencoding;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PasswordEncodingApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoder_shouldEncodePassword() {
        String encodedPassword = passwordEncoder.encode("password");

        assert !encodedPassword.equals("password");
    }

    @Test
    void passwordEncoder_shouldMatchCorrectPassword() {
        String encodedPassword = passwordEncoder.encode("password");

        assert passwordEncoder.matches("password", encodedPassword);
    }

    @Test
    void passwordEncoder_shouldRejectIncorrectPassword() {
        String encodedPassword = passwordEncoder.encode("password");

        assert !passwordEncoder.matches("wrong-password", encodedPassword);
    }

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk());
    }

    @Test
    void authenticatedEndpoint_shouldAcceptCorrectPassword() throws Exception {
        mockMvc.perform(get("/authenticated").with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(content().string("Authenticated user: user"));
    }

    @Test
    void authenticatedEndpoint_shouldRejectIncorrectPassword() throws Exception {
        mockMvc.perform(get("/authenticated").with(httpBasic("user", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }
}
