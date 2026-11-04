package com.springbyexample.v2.chunkprocessing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OrderItemProcessor}.
 *
 * @author Mujuzi Moses
 */
public class OrderItemProcessorTest {

    private final OrderItemProcessor processor = new OrderItemProcessor();

    @Test
    void process_shouldConvertOrderIdToUppercase() throws Exception {
        assertThat(processor.process("order-101")).isEqualTo("ORDER-101");
    }

    @Test
    void process_shouldUseLocaleIndependentUppercase() throws Exception {
        Locale originalLocale = Locale.getDefault();

        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertThat(processor.process("order-item"))
                    .isEqualTo("ORDER-ITEM");
        } finally {
            Locale.setDefault(originalLocale);
        }
    }
}
