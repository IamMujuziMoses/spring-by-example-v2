package com.springbyexample.v2.cloudnative.integrationtests.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.springbyexample.v2.cloudnative.configclient.config.AppProperties;
import com.springbyexample.v2.cloudnative.configclient.controller.ConfigurationController;

/**
 * Integration tests for the ConfigurationController.
 *
 * @author Mujuzi Moses
 */
public class ConfigurationControllerIT {

    @Test
    void shouldReturnApplicationConfiguration() throws Exception {
        AppProperties properties = new AppProperties("Cloud Native Configuration Demo", "staging",
                true, "example-token");

        MockMvc mockMvc = createMockMvc(properties);

        mockMvc.perform(get("/api/configuration")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cloud Native Configuration Demo"))
                .andExpect(jsonPath("$.environment").value("staging"))
                .andExpect(jsonPath("$.featureEnabled").value(true))
                .andExpect(jsonPath("$.apiTokenConfigured").value(true));
    }

    @Test
    void shouldReportApiTokenAsNotConfiguredWhenTokenIsNull() throws Exception {
        AppProperties properties = new AppProperties("Cloud Native Configuration Demo", "staging",
                true, null);

        MockMvc mockMvc = createMockMvc(properties);

        mockMvc.perform(get("/api/configuration")).andExpect(status().isOk())
                .andExpect(jsonPath("$.apiTokenConfigured").value(false));
    }

    @Test
    void shouldReportApiTokenAsNotConfiguredWhenTokenIsBlank() throws Exception {
        AppProperties properties = new AppProperties("Cloud Native Configuration Demo", "staging",
                true, "   ");

        MockMvc mockMvc = createMockMvc(properties);

        mockMvc.perform(get("/api/configuration")).andExpect(status().isOk())
                .andExpect(jsonPath("$.apiTokenConfigured").value(false));
    }

    @Test
    void shouldReturnConfigurationWhenFeatureIsDisabled() throws Exception {
        AppProperties properties = new AppProperties("Cloud Native Configuration Demo", "staging",
                false, "example-token");

        MockMvc mockMvc = createMockMvc(properties);

        mockMvc.perform(get("/api/configuration")).andExpect(status().isOk())
                .andExpect(jsonPath("$.featureEnabled").value(false))
                .andExpect(jsonPath("$.apiTokenConfigured").value(true));
    }

    private MockMvc createMockMvc(AppProperties properties) {
        return MockMvcBuilders.standaloneSetup(new ConfigurationController(properties)).build();
    }
}
