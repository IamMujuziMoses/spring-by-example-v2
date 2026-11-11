package com.springbyexample.v2.channeladapters.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;

/**
 * Unit tests for order file processing.
 *
 * @author Mujuzi Moses
 */
public class OrderFileProcessorTest {

    private final OrderFileProcessor processor = new OrderFileProcessor();

    @Test
    void orderFileProcessor_shouldConvertContentToUppercase() throws IOException {
        // Given
        String content = "order id: 1001, status: pending";
        Path inputFile = Files.createTempFile("order", ".txt");

        Files.writeString(inputFile, content);

        // When
        Message<String> result = processor.process(inputFile.toFile());

        // Then
        assertThat(result.getPayload()).isEqualTo("ORDER ID: 1001, STATUS: PENDING");
        assertThat(result.getHeaders().get("file_name")).isEqualTo("processed-" + inputFile.getFileName());
    }
}
