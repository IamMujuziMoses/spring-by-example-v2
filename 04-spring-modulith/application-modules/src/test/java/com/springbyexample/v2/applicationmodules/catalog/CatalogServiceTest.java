package com.springbyexample.v2.applicationmodules.catalog;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.api.Test;

/**
 * @author Mujuzi Moses
 */
public class CatalogServiceTest {

    private final CatalogService catalogService = new CatalogService();

    @Test
    void shouldReturnFeaturedProducts() {
        assertThat(catalogService.findFeaturedProducts()).containsExactly("Laptop", "Keyboard", "Monitor");
    }
}
