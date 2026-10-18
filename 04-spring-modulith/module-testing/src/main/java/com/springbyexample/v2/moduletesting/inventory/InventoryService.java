package com.springbyexample.v2.moduletesting.inventory;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.moduletesting.catalog.CatalogItem;
import com.springbyexample.v2.moduletesting.catalog.CatalogService;

/**
 * @author Mujuzi Moses
 */
@Service
public class InventoryService {

    private final CatalogService catalogService;

    public InventoryService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public CatalogItem findCatalogItem(String sku) {
        return catalogService.findBySku(sku);
    }
}
