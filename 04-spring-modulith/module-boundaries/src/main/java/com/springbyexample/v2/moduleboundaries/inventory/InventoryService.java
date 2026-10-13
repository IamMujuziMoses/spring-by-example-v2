package com.springbyexample.v2.moduleboundaries.inventory;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.moduleboundaries.catalog.api.Product;
import com.springbyexample.v2.moduleboundaries.catalog.api.ProductCatalog;

/**
 * @author Mujuzi Moses
 */
@Service
public class InventoryService {

    private final ProductCatalog productCatalog;

    public InventoryService(ProductCatalog productCatalog) {
        this.productCatalog = productCatalog;
    }

    public Optional<Product> findProduct(String sku) {
        return productCatalog.findBySku(sku);
    }
}
