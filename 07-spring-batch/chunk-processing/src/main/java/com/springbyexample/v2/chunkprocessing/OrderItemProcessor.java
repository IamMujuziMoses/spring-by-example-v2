package com.springbyexample.v2.chunkprocessing;

import java.util.Locale;

import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Normalizes order identifiers before they are written.
 *
 * @author Mujuzi Moses
 */
public class OrderItemProcessor implements ItemProcessor<String, String> {

    @Override
    public String process(String orderId) {
        return orderId.toUpperCase(Locale.ROOT);
    }
}
