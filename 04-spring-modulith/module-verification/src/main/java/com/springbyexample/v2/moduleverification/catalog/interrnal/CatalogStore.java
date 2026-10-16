package com.springbyexample.v2.moduleverification.catalog.interrnal;

import org.springframework.stereotype.Component;

/**
 * @author Mujuzi Moses
 */
@Component
public class CatalogStore {

    public String findNameBySku(String sku) {
        return "Sample item";
    }
}
