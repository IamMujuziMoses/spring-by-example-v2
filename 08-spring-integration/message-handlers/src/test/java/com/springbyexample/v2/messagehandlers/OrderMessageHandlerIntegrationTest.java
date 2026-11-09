package com.springbyexample.v2.messagehandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.messagehandlers.handler.OrderMessageHandler;
import com.springbyexample.v2.messagehandlers.model.OrderMessage;
import com.springbyexample.v2.messagehandlers.publisher.OrderMessagePublisher;
import com.springbyexample.v2.messagehandlers.service.OrderMessageService;
import com.springbyexample.v2.messagehandlers.support.MessageHandlerSupport;
import com.springbyexample.v2.messagehandlers.support.MessageHandlerSupport.ProcessedOrder;

/**
 * Integration tests for custom Spring Integration message handlers.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class OrderMessageHandlerIntegrationTest {

    @Autowired
    private OrderMessagePublisher publisher;

    @Autowired
    private MessageHandlerSupport support;

    @BeforeEach
    void setUp() {
        support.clear();
    }

    @Test
    void publishOrder_shouldDeliverMessageToHandler() {
        OrderMessage order = new OrderMessage(1L, "Order 1");

        boolean sent = publisher.publish(order, "web");

        assertThat(sent).isTrue();
        assertThat(support.processedOrders()).containsExactly(new ProcessedOrder(order, "web"));
    }

    @Test
    void publishMultipleOrders_shouldProcessEveryMessage() {
        OrderMessage firstOrder = new OrderMessage(1L, "Order 1");
        OrderMessage secondOrder = new OrderMessage(2L, "Order 2");

        publisher.publish(firstOrder, "web");
        publisher.publish(secondOrder, "batch");

        assertThat(support.processedOrders()).containsExactly(
                        new ProcessedOrder(firstOrder, "web"), new ProcessedOrder(secondOrder, "batch"));
    }

    @Test
    void publishOrder_shouldPreserveMessageHeaders() {
        OrderMessage order = new OrderMessage(3L, "Order 3");

        publisher.publish(order, "mobile");

        List<ProcessedOrder> processedOrders = support.processedOrders();

        assertThat(processedOrders).hasSize(1);
        assertThat(processedOrders.getFirst().order()).isEqualTo(order);
        assertThat(processedOrders.getFirst().source()).isEqualTo("mobile");
    }

    @Test
    void handleMessage_shouldThrowExceptionForUnsupportedPayload() {
        OrderMessageService orderMessageService = mock(OrderMessageService.class);

        OrderMessageHandler handler = new OrderMessageHandler(orderMessageService);

        Message<String> message = MessageBuilder.withPayload("Unsupported payload").build();

        assertThatThrownBy(() -> handler.handleMessage(message)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported message payload");
        verifyNoInteractions(orderMessageService);
    }
}
