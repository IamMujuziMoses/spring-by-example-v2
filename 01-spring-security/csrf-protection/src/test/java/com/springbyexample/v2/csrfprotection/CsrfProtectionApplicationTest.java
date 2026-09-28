package com.springbyexample.v2.csrfprotection;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class CsrfProtectionApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authenticatedRequestWithoutCsrfToken_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/transfer").with(httpBasic("user", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedRequestWithCsrfToken_shouldBeAllowed() throws Exception {
        mockMvc.perform(post("/transfer").with(httpBasic("user", "password")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedRequestWithoutCsrfToken_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/transfer")).andExpect(status().isForbidden());
    }
}
