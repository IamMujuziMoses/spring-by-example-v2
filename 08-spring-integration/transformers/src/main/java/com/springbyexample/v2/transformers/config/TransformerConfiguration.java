package com.springbyexample.v2.transformers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

import com.springbyexample.v2.transformers.support.TransformerSupport;

/**
 * Configures the input and output channels used by the transformer.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class TransformerConfiguration {

    @Bean
    public MessageChannel orderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel orderOutputChannel(TransformerSupport support) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(support);

        return channel;
    }

}
