package com.springbyexample.v2.filters.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.filters.model.OrderMessage;
import com.springbyexample.v2.filters.publisher.OrderMessagePublisher;
import com.springbyexample.v2.filters.support.FilterSupport;

/**
 * Integration tests for the order message filter.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class OrderMessageFilterIntegrationTest {

    @Autowired
    private OrderMessagePublisher publisher;

    @Autowired
    private FilterSupport filterSupport;

    @BeforeEach
    void setUp() {
        filterSupport.clear();
    }

    @Test
    void publishOrder_shouldForwardMessageWhenQuantityIsPositive() {
        OrderMessage order = new OrderMessage(1L, "Laptop", 2);

        publisher.publish(order, "web");

        assertThat(filterSupport.getProcessedOrders()).containsExactly(new FilterSupport.ProcessedOrder(order, "web"));
    }

    @Test
    void publishOrder_shouldRejectMessageWhenQuantityIsZero() {
        OrderMessage order = new OrderMessage(2L, "Keyboard", 0);

        publisher.publish(order, "web");

        assertThat(filterSupport.getProcessedOrders()).isEmpty();
    }

    @Test
    void publishMultipleOrders_shouldForwardOnlyAcceptedMessages() {
        OrderMessage acceptedOrder = new OrderMessage(1L, "Laptop", 2);
        OrderMessage rejectedOrder = new OrderMessage(2L, "Keyboard", 0);
        OrderMessage anotherAcceptedOrder = new OrderMessage(3L, "Monitor", 1);

        publisher.publish(acceptedOrder, "web");
        publisher.publish(rejectedOrder, "mobile");
        publisher.publish(anotherAcceptedOrder, "mobile");

        assertThat(filterSupport.getProcessedOrders()).containsExactly(
                        new FilterSupport.ProcessedOrder(acceptedOrder, "web"),
                        new FilterSupport.ProcessedOrder(anotherAcceptedOrder, "mobile"));
    }

    @Test
    void handleMessage_shouldThrowExceptionForUnsupportedPayload() {
        Message<String> message = MessageBuilder.withPayload("Unsupported payload").build();

        assertThatThrownBy(() -> filterSupport.handleMessage(message)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported message payload");
        assertThat(filterSupport.getProcessedOrders()).isEmpty();
    }
}
