package com.springbyexample.v2.errorchannels.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configures the asynchronous order channel and dedicated error channel.
 *
 * @author Mujuzi Moses
 */
@Configuration
@EnableIntegration
public class ErrorChannelConfiguration {

    /**
     * Creates the executor used to dispatch order messages asynchronously.
     *
     * @return configured task executor
     */
    @Bean
    public ThreadPoolTaskExecutor orderTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("order-");

        return executor;
    }

    /**
     * Creates the asynchronous order input channel.
     *
     * @param orderTaskExecutor executor used for message dispatch
     * @return asynchronous order channel
     */
    @Bean
    public MessageChannel orderInputChannel(ThreadPoolTaskExecutor orderTaskExecutor) {
        return new ExecutorChannel(orderTaskExecutor);
    }

    /**
     * Creates the channel used to receive asynchronous processing errors.
     *
     * @return dedicated error channel
     */
    @Bean
    public MessageChannel orderErrorChannel() {
        return new DirectChannel();
    }
}
