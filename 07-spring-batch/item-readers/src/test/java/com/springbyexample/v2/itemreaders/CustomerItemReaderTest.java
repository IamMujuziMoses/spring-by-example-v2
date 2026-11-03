package com.springbyexample.v2.itemreaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests the behavior of the customer item reader.
 *
 * @author Mujuzi Moses
 */
public class CustomerItemReaderTest {

    /**
     * Verifies that the reader returns customer names sequentially and
     * returns null when all items have been read.
     *
     * @throws Exception if reading an item fails
     */
    @Test
    void shouldReadItemsSequentiallyUntilInputIsExhausted() throws Exception {
        CustomerItemReader reader = new CustomerItemReader(List.of("Alice", "Bob", "Charlie"));

        assertThat(reader.read()).isEqualTo("Alice");
        assertThat(reader.read()).isEqualTo("Bob");
        assertThat(reader.read()).isEqualTo("Charlie");
        assertThat(reader.read()).isNull();
        assertThat(reader.read()).isNull();
    }

    @Test
    void shouldReturnNullWhenNoItemsAreAvailable() throws Exception {
        CustomerItemReader reader = new CustomerItemReader(List.of());

        assertThat(reader.read()).isNull();
    }

    @Test
    void shouldRejectNullCustomerNames() {
        assertThatNullPointerException().isThrownBy(() -> new CustomerItemReader(null))
                .withMessage("Customer names must not be null");
    }

}
