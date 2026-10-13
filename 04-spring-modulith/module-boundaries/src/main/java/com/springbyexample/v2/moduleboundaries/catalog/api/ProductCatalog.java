package com.springbyexample.v2.moduleboundaries.catalog.api;

import java.util.Optional;

/**
 * @author Mujuzi Moses
 */
public interface ProductCatalog {

    Optional<Product> findBySku(String sku);
}
