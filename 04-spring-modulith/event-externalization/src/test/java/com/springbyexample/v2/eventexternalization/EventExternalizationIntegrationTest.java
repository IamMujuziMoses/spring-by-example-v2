package com.springbyexample.v2.eventexternalization;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.messaging.Message;

import com.springbyexample.v2.eventexternalization.orders.OrderCompleted;
import com.springbyexample.v2.eventexternalization.orders.OrderService;

/**
 * @author Mujuzi Moses
 */
@SpringBootTest
@Import(TestKafkaConfiguration.class)
public class EventExternalizationIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private KafkaOperations<Object, Object> kafkaOperations;


    @Test
    void shouldExternalizeCompletedOrderEvent() {
        orderService.completeOrder("ORDER-001");

        verify(kafkaOperations, timeout(5000)).send(argThat((Message<?> message) -> "orders.completed".equals(
                message.getHeaders().get("kafka_topic"))
                && message.getPayload() instanceof OrderCompleted
                && "ORDER-001".equals(((OrderCompleted) message.getPayload()).orderId())));
    }
}