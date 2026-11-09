package com.springbyexample.v2.messageendpoints.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;

/**
 * Configures the message channel used by the order message endpoint.
 *
 * @author Mujuzi Moses
 */
@Configuration
@EnableIntegration
public class MessageEndpointConfiguration {

    @Bean
    public MessageChannel orderEndpointChannel() {
        return new DirectChannel();
    }
}
