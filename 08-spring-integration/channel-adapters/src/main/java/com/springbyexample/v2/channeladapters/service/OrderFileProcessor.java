package com.springbyexample.v2.channeladapters.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Processes files received through the inbound channel adapter.
 *
 * @author Mujuzi Moses
 */
@Component
public class OrderFileProcessor {

    @ServiceActivator(inputChannel = "fileInputChannel", outputChannel = "fileOutputChannel")
    public Message<String> process(File file) throws IOException {
        String content = Files.readString(file.toPath());
        String processedContent = content.toUpperCase(Locale.ROOT);

        return MessageBuilder.withPayload(processedContent).setHeader("file_name", "processed-"
                        + file.getName()).build();
    }
}
