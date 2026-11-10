package com.springbyexample.v2.routers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.springbyexample.v2.routers.model.OrderMessage;
import com.springbyexample.v2.routers.model.OrderType;
import com.springbyexample.v2.routers.publisher.OrderMessagePublisher;
import com.springbyexample.v2.routers.support.OrderRoutingSupport;
import com.springbyexample.v2.routers.support.OrderRoutingSupport.RoutedOrder;

/**
 * Integration tests for order message routing.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
public class OrderMessageRouterIntegrationTest {

    @Autowired
    private OrderMessagePublisher publisher;

    @Autowired
    private OrderRoutingSupport support;

    @BeforeEach
    void setUp() {
        support.clear();
    }

    @Test
    void publishRetailOrder_shouldRouteMessageToRetailChannel() {
        OrderMessage order = new OrderMessage(1L, "Laptop", 2, OrderType.RETAIL);

        publisher.publish(order);

        assertThat(support.getRoutedOrders()).containsExactly(new RoutedOrder(order, "RETAIL"));
    }

    @Test
    void publishWholesaleOrder_shouldRouteMessageToWholesaleChannel() {
        OrderMessage order = new OrderMessage(2L, "Keyboard", 50, OrderType.WHOLESALE);

        publisher.publish(order);

        assertThat(support.getRoutedOrders()).containsExactly(new RoutedOrder(order, "WHOLESALE"));
    }

    @Test
    void publishMultipleOrders_shouldRouteEachMessageToItsDestination() {
        OrderMessage retailOrder = new OrderMessage(1L, "Laptop", 2, OrderType.RETAIL);
        OrderMessage wholesaleOrder = new OrderMessage(2L, "Keyboard", 50, OrderType.WHOLESALE);

        publisher.publish(retailOrder);
        publisher.publish(wholesaleOrder);

        assertThat(support.getRoutedOrders()).containsExactly(
                new RoutedOrder(retailOrder, "RETAIL"), new RoutedOrder(wholesaleOrder, "WHOLESALE"));
    }

    @Test
    void record_shouldThrowExceptionForUnsupportedPayload() {
        Message<String> message = MessageBuilder.withPayload("Unsupported payload").build();

        assertThatThrownBy(() -> support.record(message, "RETAIL")).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported message payload");
        assertThat(support.getRoutedOrders()).isEmpty();
    }
}
