package com.springbyexample.v2.eventexternalization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaOperations;

/**
 * @author Mujuzi Moses
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestKafkaConfiguration {

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    KafkaOperations<Object, Object> kafkaOperations() {
        KafkaOperations<Object, Object> operations =
                mock(KafkaOperations.class);

        when(operations.send(anyString(), any(), any())).thenAnswer(invocation -> {
                    System.out.println("Kafka send called: " + invocation.getArgument(0));
                    return CompletableFuture.completedFuture(null);
                });

        return operations;
    }
}
