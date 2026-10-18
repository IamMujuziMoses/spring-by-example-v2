package com.springbyexample.v2.moduletesting.catalog;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.moduletesting.catalog.internal.CatalogStore;

/**
 * @author Mujuzi Moses
 */
@Service
public class CatalogService {

    private final CatalogStore catalogStore;

    public CatalogService(CatalogStore catalogStore) {
        this.catalogStore = catalogStore;
    }

    public CatalogItem findBySku(String sku) {
        return new CatalogItem(sku, catalogStore.findNameBySku(sku)
        );
    }
}
