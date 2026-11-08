package com.springbyexample.v2.messagechannels.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;

import com.springbyexample.v2.messagechannels.handler.DirectOrderMessageHandler;
import com.springbyexample.v2.messagechannels.handler.OrderAuditMessageHandler;
import com.springbyexample.v2.messagechannels.handler.OrderNotificationMessageHandler;

/**
 * Configures the message channels used by the application.
 *
 * @author Mujuzi Moses
 */
@Configuration
public class MessageChannelConfiguration {

    /**
     * Creates a point-to-point order message channel.
     *
     * @param handler direct order message handler
     * @return configured direct channel
     */
    @Bean
    public DirectChannel orderChannel(DirectOrderMessageHandler handler) {
        DirectChannel channel = new DirectChannel();
        channel.subscribe(handler);
        return channel;
    }

    /**
     * Creates a publish-subscribe order events channel.
     *
     * @param auditHandler        order audit message handler
     * @param notificationHandler order notification message handler
     * @return configured publish-subscribe channel
     */
    @Bean
    public PublishSubscribeChannel orderEventsChannel(OrderAuditMessageHandler auditHandler,
            OrderNotificationMessageHandler notificationHandler) {

        PublishSubscribeChannel channel = new PublishSubscribeChannel();

        channel.subscribe(auditHandler);
        channel.subscribe(notificationHandler);

        return channel;
    }

}
