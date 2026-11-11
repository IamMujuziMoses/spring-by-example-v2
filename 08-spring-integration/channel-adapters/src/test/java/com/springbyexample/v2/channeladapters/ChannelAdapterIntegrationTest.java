package com.springbyexample.v2.channeladapters;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integration tests for inbound and outbound file channel adapters.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ChannelAdapterIntegrationTest {

    private static final Path TEST_DIRECTORY = createTestDirectory();

    private static final Path INPUT_DIRECTORY = TEST_DIRECTORY.resolve("input");

    private static final Path OUTPUT_DIRECTORY = TEST_DIRECTORY.resolve("output");

    @Autowired
    @Qualifier("fileOutputChannel")
    private MessageChannel fileOutputChannel;

    @DynamicPropertySource
    static void registerFileDirectories(DynamicPropertyRegistry registry) {
        registry.add("app.files.input-directory", INPUT_DIRECTORY::toString);
        registry.add("app.files.output-directory", OUTPUT_DIRECTORY::toString);
    }

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(INPUT_DIRECTORY);
        Files.createDirectories(OUTPUT_DIRECTORY);

        cleanDirectory(INPUT_DIRECTORY);
        cleanDirectory(OUTPUT_DIRECTORY);
    }

    @Test
    void inboundAdapter_shouldReadFileAndPublishMessage() throws IOException {
        // Given
        Path inputFile = INPUT_DIRECTORY.resolve("inbound-order.txt");
        Files.writeString(inputFile, "order received");

        // When
        Path outputFile = OUTPUT_DIRECTORY.resolve("processed-inbound-order.txt");

        waitForFile(outputFile);

        // Then
        assertThat(Files.exists(outputFile)).isTrue();
        assertThat(Files.readString(outputFile)).isEqualTo("ORDER RECEIVED");
    }

    @Test
    void outboundAdapter_shouldWriteProcessedMessageToFile() throws IOException {
        // Given
        String content = "PROCESSED ORDER";
        String fileName = "outbound-order.txt";

        var message = MessageBuilder.withPayload(content).setHeader("file_name", fileName).build();

        // When
        fileOutputChannel.send(message);

        // Then
        Path outputFile = OUTPUT_DIRECTORY.resolve(fileName);

        assertThat(Files.exists(outputFile)).isTrue();
        assertThat(Files.readString(outputFile)).isEqualTo(content);
    }

    @Test
    void fileProcessingFlow_shouldProcessInputAndCreateOutputFile() throws IOException {
        // Given
        String content = "order id: 1001, status: pending";
        Path inputFile = INPUT_DIRECTORY.resolve("flow-order.txt");
        Files.writeString(inputFile, content);

        // When
        Path outputFile = OUTPUT_DIRECTORY.resolve("processed-flow-order.txt");

        waitForFile(outputFile);

        // Then
        assertThat(Files.exists(outputFile)).isTrue();
        assertThat(Files.readString(outputFile)).isEqualTo("ORDER ID: 1001, STATUS: PENDING");
    }

    private static void waitForFile(Path file) throws IOException {
        long timeout = Duration.ofSeconds(5).toNanos();
        long start = System.nanoTime();

        while (System.nanoTime() - start < timeout) {
            if (Files.exists(file)) {
                return;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting for output file", exception);
            }
        }

        throw new AssertionError("Expected output file was not created: " + file);
    }

    private static void cleanDirectory(Path directory) throws IOException {

        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.toList()) {
                Files.delete(file);
            }
        }
    }

    private static Path createTestDirectory() {
        try {
            return Files.createTempDirectory("channel-adapters-test");
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
