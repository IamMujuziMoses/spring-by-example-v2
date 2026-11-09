package com.springbyexample.v2.messagehandlers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

import com.springbyexample.v2.messagehandlers.handler.OrderMessageHandler;

/**
 * Configures the message channel and its message handler.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class MessageHandlerConfiguration {

    @Bean
    public MessageChannel orderHandlerChannel(OrderMessageHandler orderMessageHandler) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(orderMessageHandler);

        return channel;
    }

}
