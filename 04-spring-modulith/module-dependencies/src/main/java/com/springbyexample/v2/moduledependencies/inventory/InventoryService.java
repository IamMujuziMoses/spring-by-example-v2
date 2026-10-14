package com.springbyexample.v2.moduledependencies.inventory;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.moduledependencies.catalog.CatalogService;
import com.springbyexample.v2.moduledependencies.catalog.Product;

/**
 * @author Mujuzi Moses
 */
@Service
public class InventoryService {

    private final CatalogService catalogService;

    public InventoryService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public Optional<Product> findProduct(String sku) {
        return catalogService.findBySku(sku);
    }
}
