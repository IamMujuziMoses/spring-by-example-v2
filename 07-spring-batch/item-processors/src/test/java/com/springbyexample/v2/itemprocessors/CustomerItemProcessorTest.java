package com.springbyexample.v2.itemprocessors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the customer item processor.
 *
 * @author Mujuzi Moses
 */
public class CustomerItemProcessorTest {

    private CustomerItemProcessor processor;

    /**
     * Creates the processor before each test.
     */
    @BeforeEach
    void setUp() {
        processor = new CustomerItemProcessor();
    }

    /**
     * Verifies that processing removes surrounding whitespace and converts
     * a customer name to uppercase.
     *
     * @throws Exception if processing fails
     */
    @Test
    void process_shouldNormalizeCustomerName() throws Exception {
        String result = processor.process(" Alice ");

        assertThat(result).isEqualTo("ALICE");
    }

    /**
     * Verifies that processing filters out a blank customer name.
     *
     * @throws Exception if processing fails
     */
    @Test
    void process_shouldReturnNullForBlankCustomerName() throws Exception {
        String result = processor.process("   ");

        assertThat(result).isNull();
    }

    /**
     * Verifies that processing preserves the content of a non-blank name
     * while normalizing its representation.
     *
     * @throws Exception if processing fails
     */
    @Test
    void process_shouldNormalizeCustomerNameWithoutChangingItsContent() throws Exception {
        String result = processor.process("Charlie");

        assertThat(result).isEqualTo("CHARLIE");
    }

    @Test
    void process_shouldReturnNullForEmptyCustomerName() throws Exception {
        String result = processor.process("");

        assertThat(result).isNull();
    }

}
