package com.springbyexample.v2.serviceactivators.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;

import com.springbyexample.v2.serviceactivators.support.OrderProcessingSupport;

/**
 * Configures the channels used by the service activator example.
 *
 * @author Mujuzi Moses
 */
@Configuration
@EnableIntegration
public class ServiceActivatorConfiguration {

    @Bean
    public MessageChannel orderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel orderOutputChannel(OrderProcessingSupport support) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(support);
        return channel;
    }

    @Bean
    public OrderProcessingSupport orderProcessingSupport() {
        return new OrderProcessingSupport();
    }
}
