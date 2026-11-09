package com.springbyexample.v2.transformers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.transformers.model.OrderMessage;
import com.springbyexample.v2.transformers.model.OrderSummary;
import com.springbyexample.v2.transformers.publisher.OrderMessagePublisher;
import com.springbyexample.v2.transformers.support.TransformerSupport;
import com.springbyexample.v2.transformers.support.TransformerSupport.ProcessedOrder;

/**
 * Integration tests for Spring Integration message transformers.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class OrderMessageTransformerIntegrationTest {

    @Autowired
    private OrderMessagePublisher publisher;

    @Autowired
    private TransformerSupport support;

    @BeforeEach
    void setUp() {
        support.clear();
    }

    @Test
    void publishOrder_shouldTransformMessageIntoOrderSummary() {
        OrderMessage order = new OrderMessage(1L, "Laptop", 2, new BigDecimal("1200.00"));

        boolean sent = publisher.publish(order, "web");

        OrderSummary expected = new OrderSummary(1L, "2 x Laptop", new BigDecimal("2400.00"));

        assertThat(sent).isTrue();
        assertThat(support.processedOrders()).containsExactly(new ProcessedOrder(expected, "web"));
    }

    @Test
    void publishMultipleOrders_shouldTransformEveryMessage() {
        OrderMessage firstOrder = new OrderMessage(1L, "Laptop", 2, new BigDecimal("1200.00"));
        OrderMessage secondOrder = new OrderMessage(2L, "Keyboard", 3, new BigDecimal("50.00"));

        publisher.publish(firstOrder, "web");
        publisher.publish(secondOrder, "mobile");

        assertThat(support.processedOrders()).containsExactly(new ProcessedOrder(new OrderSummary(1L,
                        "2 x Laptop", new BigDecimal("2400.00")), "web"),
                        new ProcessedOrder(new OrderSummary(2L, "3 x Keyboard",
                                new BigDecimal("150.00")), "mobile"));
    }

    @Test
    void publishOrder_shouldPreserveMessageHeaders() {
        OrderMessage order = new OrderMessage(3L, "Monitor", 1, new BigDecimal("350.00"));

        publisher.publish(order, "mobile");

        assertThat(support.processedOrders()).containsExactly(new ProcessedOrder(new OrderSummary(3L,
                        "1 x Monitor", new BigDecimal("350.00")), "mobile"));
    }

    @Test
    void handleMessage_shouldThrowExceptionForUnsupportedPayload() {
        Message<String> message = MessageBuilder.withPayload("Unsupported payload").build();

        assertThatThrownBy(() -> support.handleMessage(message)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported message payload");
    }
}
