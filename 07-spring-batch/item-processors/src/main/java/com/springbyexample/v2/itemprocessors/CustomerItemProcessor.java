package com.springbyexample.v2.itemprocessors;

import java.util.Locale;

import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Normalizes customer names and filters out blank entries.
 *
 * @author Mujuzi Moses
 */
public class CustomerItemProcessor implements ItemProcessor<String, String> {

    /**
     * Processes a customer name by removing surrounding whitespace and
     * converting it to uppercase.
     * <p>
     * Blank customer names are filtered out by returning {@code null}.
     *
     * @param customerName customer name to process
     * @return normalized customer name, or {@code null} when the name is blank
     */
    @Override
    public String process(String customerName) {
        String normalizedName = customerName.trim();

        if (normalizedName.isEmpty()) {
            return null;
        }

        return normalizedName.toUpperCase(Locale.ROOT);
    }

}
