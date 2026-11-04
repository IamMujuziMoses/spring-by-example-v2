package com.springbyexample.v2.itemwriters;

import java.util.Locale;

import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Normalizes customer names and filters out blank entries.
 *
 * @author Mujuzi Moses
 */
public class CustomerItemProcessor implements ItemProcessor<String, String> {

    @Override
    public String process(String customerName) {
        String normalizedName = customerName.trim();

        if (normalizedName.isEmpty()) {
            return null;
        }

        return normalizedName.toUpperCase(Locale.ROOT);
    }
}
