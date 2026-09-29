package com.springbyexample.v2.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class JwtApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void home_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string("JWT Example"));
    }

    @Test
    void publicEndpoint_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public")).andExpect(status().isOk())
                .andExpect(content().string("This endpoint is public."));
    }

    @Test
    void userEndpointWithoutToken_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/user")).andExpect(status().isUnauthorized());
    }

    @Test
    void userEndpointWithValidJwt_shouldBeAccessible() throws Exception {
        String token = createToken();

        mockMvc.perform(get("/user").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(content().string("Authenticated as: user"));
    }

    @Test
    void userEndpointWithInvalidToken_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/user").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    private String createToken() {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder().subject("user").issuedAt(now)
                .expiresAt(now.plusSeconds(300)).claim("scope", "read").build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
