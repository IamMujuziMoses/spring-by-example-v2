package com.springbyexample.v2.modulararchitecture.audit;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.modulararchitecture.audit.internal.AuditController;

/**
 * Tests the audit REST controller.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(AuditController.class)
public class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @Test
    void getAuditEntries_shouldReturnAuditEntries() throws Exception {
        given(auditService.getEntries()).willReturn(
                List.of(new AuditEntry("TASK_CREATED", "Task created: Write tests", 10L, 1L,
                        Instant.parse("2026-01-01T10:00:00Z"))));

        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventType")
                        .value("TASK_CREATED"))
                .andExpect(jsonPath("$[0].description")
                        .value("Task created: Write tests"))
                .andExpect(jsonPath("$[0].taskId").value(10))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].occurredAt")
                        .value("2026-01-01T10:00:00Z"));
    }

    @Test
    void getAuditEntries_shouldReturnEmptyArrayWhenNoEntriesExist() throws Exception {
        given(auditService.getEntries()).willReturn(List.of());

        mockMvc.perform(get("/api/audit")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
