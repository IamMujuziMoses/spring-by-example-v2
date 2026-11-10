package com.springbyexample.v2.serviceactivators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.serviceactivators.model.OrderMessage;
import com.springbyexample.v2.serviceactivators.model.OrderProcessingResult;
import com.springbyexample.v2.serviceactivators.publisher.OrderMessagePublisher;
import com.springbyexample.v2.serviceactivators.support.OrderProcessingSupport;
import com.springbyexample.v2.serviceactivators.support.OrderProcessingSupport.ProcessedOrder;

/**
 * Integration tests for the order message service activator.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class OrderMessageServiceActivatorIntegrationTest {

    @Autowired
    private OrderMessagePublisher publisher;

    @Autowired
    private OrderProcessingSupport support;

    @BeforeEach
    void setUp() {
        support.clear();
    }

    @Test
    void publishOrder_shouldProcessMessageAndPublishResult() {
        OrderMessage order = new OrderMessage(1L, "Laptop", 2, new BigDecimal("1200.00"));

        publisher.publish(order, "web");

        OrderProcessingResult expectedResult = new OrderProcessingResult(1L, "Laptop", 2,
                new BigDecimal("2400.00"));

        assertThat(support.getProcessedOrders()).containsExactly(new ProcessedOrder(expectedResult, "web"));
    }

    @Test
    void publishMultipleOrders_shouldProcessEveryMessage() {
        OrderMessage laptop = new OrderMessage(1L, "Laptop", 2, new BigDecimal("1200.00"));
        OrderMessage keyboard = new OrderMessage(2L, "Keyboard", 5, new BigDecimal("50.00"));

        publisher.publish(laptop, "web");
        publisher.publish(keyboard, "mobile");

        assertThat(support.getProcessedOrders()).containsExactly(
                new ProcessedOrder(new OrderProcessingResult(1L, "Laptop", 2,
                        new BigDecimal("2400.00")), "web"),
                        new ProcessedOrder(new OrderProcessingResult(2L, "Keyboard", 5,
                                new BigDecimal("250.00")), "mobile"));
    }

    @Test
    void publishOrder_shouldPreserveMessageHeaders() {
        OrderMessage order = new OrderMessage(3L, "Monitor", 3, new BigDecimal("300.00"));

        publisher.publish(order, "mobile");

        assertThat(support.getProcessedOrders()).singleElement().extracting(ProcessedOrder::source)
                .isEqualTo("mobile");
    }

    @Test
    void handleMessage_shouldThrowExceptionForUnsupportedPayload() {
        Message<String> message = MessageBuilder.withPayload("Unsupported payload").build();

        assertThatThrownBy(() -> support.handleMessage(message)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported message payload");

        assertThat(support.getProcessedOrders()).isEmpty();
    }
}
