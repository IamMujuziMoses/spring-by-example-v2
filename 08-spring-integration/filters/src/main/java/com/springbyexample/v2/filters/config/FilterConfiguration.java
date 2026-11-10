package com.springbyexample.v2.filters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;

import com.springbyexample.v2.filters.support.FilterSupport;

/**
 * Configures message channels for the filters example.
 *
 * @author Mujuzi Moses
 */
@Configuration
@EnableIntegration
public class FilterConfiguration {

    @Bean
    public MessageChannel orderInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel orderOutputChannel(FilterSupport support) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(support);

        return channel;
    }
}
