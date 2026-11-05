package com.springbyexample.v2.jobexecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

/**
 * Integration tests for Spring Batch job execution and metadata.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
@AutoConfigureMockMvc
public class JobExecutionJobConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void launchJob_shouldCompleteAndExposeExecutionMetadata() throws Exception {

        mockMvc.perform(post("/jobs/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").isNumber())
                .andExpect(jsonPath("$.jobName").value("jobExecutionMetadataJob"))
                .andExpect(jsonPath("$.jobInstanceId").isNumber())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.startTime").isNotEmpty())
                .andExpect(jsonPath("$.endTime").isNotEmpty())
                .andExpect(jsonPath("$.exitStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps.length()").value(1))
                .andExpect(jsonPath("$.steps[0].stepName").value("processMetadataStep"))
                .andExpect(jsonPath("$.steps[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.steps[0].processedRecords").value(5));
    }

    @Test
    void getExecution_shouldReturnStoredExecutionMetadata() throws Exception {

        MvcResult launchResult = mockMvc.perform(post("/jobs/executions")).andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").isNumber()).andReturn();

        String response = launchResult.getResponse().getContentAsString();

        long executionId = ((Number) JsonPath.read(response, "$.executionId")).longValue();

        mockMvc.perform(get("/jobs/executions/{executionId}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId))
                .andExpect(jsonPath("$.jobName").value("jobExecutionMetadataJob"))
                .andExpect(jsonPath("$.jobInstanceId").isNumber())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.exitStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.steps[0].stepName").value("processMetadataStep"))
                .andExpect(jsonPath("$.steps[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.steps[0].processedRecords").value(5));
    }

    @Test
    void getExecution_shouldReturnNotFoundForUnknownExecution() throws Exception {
        mockMvc.perform(get("/jobs/executions/{executionId}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void launchJob_shouldCreateDistinctJobInstances() throws Exception {

        MvcResult firstLaunch = mockMvc.perform(post("/jobs/executions")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED")).andReturn();
        MvcResult secondLaunch = mockMvc.perform(post("/jobs/executions")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED")).andReturn();

        String firstResponse = firstLaunch.getResponse().getContentAsString();
        String secondResponse = secondLaunch.getResponse().getContentAsString();

        long firstExecutionId = ((Number) JsonPath.read(firstResponse, "$.executionId")).longValue();
        long secondExecutionId = ((Number) JsonPath.read(secondResponse, "$.executionId")).longValue();
        long firstJobInstanceId = ((Number) JsonPath.read(firstResponse, "$.jobInstanceId")).longValue();
        long secondJobInstanceId = ((Number) JsonPath.read(secondResponse, "$.jobInstanceId")).longValue();

        String firstRunId = JsonPath.read(firstResponse, "$.jobParameters.parameters[0].value");
        String secondRunId = JsonPath.read(secondResponse, "$.jobParameters.parameters[0].value");

        assertThat(firstRunId).isNotEqualTo(secondRunId);
        assertThat(firstExecutionId).isNotEqualTo(secondExecutionId);
        assertThat(firstJobInstanceId).isNotEqualTo(secondJobInstanceId);
    }

    @Test
    void launchJob_shouldExposeStepExecutionMetadata() throws Exception {

        mockMvc.perform(post("/jobs/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps.length()").value(1))
                .andExpect(jsonPath("$.steps[0].stepExecutionId").isNumber())
                .andExpect(jsonPath("$.steps[0].stepName").value("processMetadataStep"))
                .andExpect(jsonPath("$.steps[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.steps[0].startTime").isNotEmpty())
                .andExpect(jsonPath("$.steps[0].endTime").isNotEmpty())
                .andExpect(jsonPath("$.steps[0].readCount").value(0))
                .andExpect(jsonPath("$.steps[0].writeCount").value(0))
                .andExpect(jsonPath("$.steps[0].commitCount").value(1))
                .andExpect(jsonPath("$.steps[0].rollbackCount").value(0))
                .andExpect(jsonPath("$.steps[0].processedRecords").value(5));
    }
}