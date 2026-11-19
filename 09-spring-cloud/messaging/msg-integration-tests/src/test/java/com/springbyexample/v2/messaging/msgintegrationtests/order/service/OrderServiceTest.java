package com.springbyexample.v2.messaging.msgintegrationtests.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.messaging.msgorderservice.event.OrderCreatedEvent;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderAcceptedResponse;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderRequest;
import com.springbyexample.v2.messaging.msgorderservice.service.OrderEventPublisher;
import com.springbyexample.v2.messaging.msgorderservice.service.OrderService;

/**
 * Unit tests for {@link OrderService}.
 *
 * @author Mujuzi Moses
 */
public class OrderServiceTest {

    private OrderEventPublisher orderEventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderEventPublisher = mock(OrderEventPublisher.class);
        orderService = new OrderService(orderEventPublisher);
    }

    @Test
    void shouldCreateOrderAndPublishEvent() {
        given(orderEventPublisher.publish(org.mockito.ArgumentMatchers.any(OrderCreatedEvent.class))).willReturn(true);

        OrderRequest request = new OrderRequest("order-001", "SKU-1001", 2);

        OrderAcceptedResponse response = orderService.createOrder(request);

        assertThat(response.orderId()).isEqualTo("order-001");
        assertThat(response.status()).isEqualTo("ACCEPTED");

        verify(orderEventPublisher).publish(argThat(event ->
                event.orderId().equals("order-001") && event.productId().equals("SKU-1001") && event.quantity() == 2
                        && event.eventId() != null && event.occurredAt() != null));
    }

    @Test
    void shouldThrowExceptionWhenPublishingFails() {
        given(orderEventPublisher.publish(org.mockito.ArgumentMatchers.any(OrderCreatedEvent.class))).willReturn(false);

        OrderRequest request = new OrderRequest("order-001", "SKU-1001", 2);

        assertThatThrownBy(() -> orderService.createOrder(request)).isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to publish order-created event");
    }
}
