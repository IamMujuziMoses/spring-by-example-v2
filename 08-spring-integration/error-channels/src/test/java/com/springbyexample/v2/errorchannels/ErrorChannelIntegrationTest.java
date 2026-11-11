package com.springbyexample.v2.errorchannels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.messaging.MessageChannel;

import com.springbyexample.v2.errorchannels.handler.ErrorMessageHandler;
import com.springbyexample.v2.errorchannels.model.ErrorDetails;
import com.springbyexample.v2.errorchannels.model.OrderMessage;
import com.springbyexample.v2.errorchannels.publisher.OrderPublisher;

/**
 * Integration tests for Spring Integration error channels.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class ErrorChannelIntegrationTest {

    private static final Duration ERROR_TIMEOUT = Duration.ofSeconds(5);

    private static final Duration SUCCESS_TIMEOUT = Duration.ofMillis(500);

    @Autowired
    private OrderPublisher orderPublisher;

    @Autowired
    private ErrorMessageHandler errorMessageHandler;

    @Autowired
    @Qualifier("orderInputChannel")
    private MessageChannel orderInputChannel;

    @BeforeEach
    void setUp() {
        errorMessageHandler.clear();
    }

    @Test
    void asynchronousProcessing_shouldPublishErrorMessageToErrorChannel() throws InterruptedException {
        OrderMessage order = new OrderMessage("ORDER-001", 0);
        boolean accepted = orderPublisher.publish(order);

        assertTrue(accepted);

        ErrorDetails error = errorMessageHandler.awaitError(ERROR_TIMEOUT);

        assertNotNull(error);
        assertEquals(order, error.failedOrder());
        assertEquals("IllegalArgumentException", error.exceptionType());
        assertEquals("Order quantity must be greater than zero", error.exceptionMessage());
    }

    @Test
    void orderInputChannel_shouldUseExecutorChannel() {
        assertInstanceOf(ExecutorChannel.class, orderInputChannel);
    }

    @Test
    void successfulProcessing_shouldNotPublishErrorMessage() throws InterruptedException {
        OrderMessage order = new OrderMessage("ORDER-002", 2);
        boolean accepted = orderPublisher.publish(order);

        assertTrue(accepted);

        ErrorDetails error = errorMessageHandler.awaitError(SUCCESS_TIMEOUT);

        assertNull(error);
    }
}
