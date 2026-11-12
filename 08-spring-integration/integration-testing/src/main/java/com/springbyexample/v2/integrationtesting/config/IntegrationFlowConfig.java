package com.springbyexample.v2.integrationtesting.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.springbyexample.v2.integrationtesting.service.OrderProcessingService;
import com.springbyexample.v2.integrationtesting.service.OrderTransformer;

/**
 * Configures the integration flows used by the Integration Testing example.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class IntegrationFlowConfig {

    @Bean
    public MessageChannel ordersInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public QueueChannel ordersOutputChannel() {
        return new QueueChannel();
    }

    @Bean
    public TaskExecutor orderTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("order-async-");

        return executor;
    }

    @Bean
    public MessageChannel asyncOrdersInputChannel(@Qualifier("orderTaskExecutor") TaskExecutor executor) {
        return new ExecutorChannel(executor);
    }

    @Bean
    public QueueChannel asyncOrdersOutputChannel() {
        return new QueueChannel();
    }

    @Bean
    public IntegrationFlow orderIntegrationFlow(OrderTransformer orderTransformer,
            OrderProcessingService orderProcessingService) {

        return IntegrationFlow.from("ordersInputChannel").transform(orderTransformer)
                .handle(orderProcessingService, "process").channel("ordersOutputChannel")
                .get();
    }

    @Bean
    public IntegrationFlow asyncOrderIntegrationFlow(OrderTransformer orderTransformer,
            OrderProcessingService orderProcessingService) {

        return IntegrationFlow.from("asyncOrdersInputChannel").transform(orderTransformer)
                .handle(orderProcessingService, "process").channel("asyncOrdersOutputChannel")
                .get();
    }
}
