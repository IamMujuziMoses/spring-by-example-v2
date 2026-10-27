package com.springbyexample.v2.applicationdesignandarchitecture.domain.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests for the TaskId domain value object.
 *
 * @author Mujuzi Moses
 */
public class TaskIdDomainTest {

    @Test
    void shouldCreateTaskIdWithValidValue() {
        TaskId taskId = new TaskId("task-1");

        assertEquals("task-1", taskId.value());
    }

    @Test
    void shouldRejectBlankTaskId() {
        assertThrows(IllegalArgumentException.class, () -> new TaskId(" "));
    }
}