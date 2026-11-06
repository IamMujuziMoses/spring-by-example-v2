package com.springbyexample.v2.batchtesting.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.springbyexample.v2.batchtesting.BatchTestingApplication;
import com.springbyexample.v2.batchtesting.domain.Order;
import com.springbyexample.v2.batchtesting.reader.OrderItemReader;
import com.springbyexample.v2.batchtesting.support.BatchTestingSupport;

/**
 * Tests the step-scoped order reader using Spring Batch test support.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest(classes = BatchTestingApplication.class)
@SpringBatchTest
public class StepScopedOrderItemReaderTest {

    @Autowired
    private OrderItemReader reader;

    @Autowired
    private BatchTestingSupport support;

    @BeforeEach
    void setUp() {
        support.reset();
    }

    @Test
    void read_shouldReadOrdersWithinStepScope() {
        Order order = reader.read();

        assertThat(order).isEqualTo(new Order(1L, "first order"));
    }
}
