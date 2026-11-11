package com.springbyexample.v2.channeladapters.config;

import java.io.File;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.inbound.FileReadingMessageSource;
import org.springframework.integration.file.outbound.FileWritingMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * Configuration for inbound and outbound file channel adapters.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class ChannelAdapterConfiguration {

    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel fileOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    @InboundChannelAdapter(channel = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
    public MessageSource<File> fileReadingMessageSource(@Value("${app.files.input-directory}") File inputDirectory) {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(inputDirectory);

        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "fileOutputChannel")
    public MessageHandler fileWritingMessageHandler(@Value("${app.files.output-directory}") File outputDirectory) {
        FileWritingMessageHandler handler = new FileWritingMessageHandler(outputDirectory);
        handler.setExpectReply(false);
        handler.setFileNameGenerator(message -> (String) Objects.requireNonNull(message.getHeaders()
                .get("file_name")));

        return handler;
    }
}
