package com.springbyexample.v2.springsecurity.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tests the password encoder and configured in-memory users.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class SecurityConfigurationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void shouldUseBCryptPasswordEncoder() {
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
    }

    @Test
    void shouldEncodePasswordsAndVerifyThem() {
        String encodedPassword = passwordEncoder.encode("reader-password");

        assertTrue(passwordEncoder.matches("reader-password", encodedPassword));
        assertFalse(passwordEncoder.matches("wrong-password", encodedPassword));
        assertNotEquals("reader-password", encodedPassword);
    }

    @Test
    void shouldConfigureReaderWithUserRole() {
        UserDetails reader = userDetailsService.loadUserByUsername("reader");

        assertTrue(reader.getAuthorities().stream().anyMatch(authority ->
                Objects.equals(authority.getAuthority(), "ROLE_USER")));
        assertFalse(reader.getAuthorities().stream().anyMatch(authority ->
                Objects.equals(authority.getAuthority(), "ROLE_ADMIN")));
        assertTrue(passwordEncoder.matches("reader-password", reader.getPassword()));
    }

    @Test
    void shouldConfigureAdminWithUserAndAdminRoles() {
        UserDetails admin = userDetailsService.loadUserByUsername("admin");

        assertTrue(admin.getAuthorities().stream().anyMatch(authority ->
                Objects.equals(authority.getAuthority(), "ROLE_USER")));
        assertTrue(admin.getAuthorities().stream().anyMatch(authority ->
                Objects.equals(authority.getAuthority(), "ROLE_ADMIN")));
        assertTrue(passwordEncoder.matches("admin-password", admin.getPassword()));
    }
}