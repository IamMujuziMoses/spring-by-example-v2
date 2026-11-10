package com.springbyexample.v2.routers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;

import com.springbyexample.v2.routers.support.OrderRoutingSupport;

/**
 * Configures the channels used by the router example.
 *
 * @author Mujuzi Moses
 */
@Configuration
@EnableIntegration
public class RouterConfiguration {

    @Bean
    public OrderRoutingSupport orderRoutingSupport() {
        return new OrderRoutingSupport();
    }

    @Bean
    public MessageChannel orderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel retailOrderChannel(OrderRoutingSupport support) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(message -> support.record(message, "RETAIL"));
        return channel;
    }

    @Bean
    public MessageChannel wholesaleOrderChannel(OrderRoutingSupport support) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(message -> support.record(message, "WHOLESALE"));
        return channel;
    }
}
