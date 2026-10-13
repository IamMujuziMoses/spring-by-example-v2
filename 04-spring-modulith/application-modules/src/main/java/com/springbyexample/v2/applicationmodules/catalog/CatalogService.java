package com.springbyexample.v2.applicationmodules.catalog;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * @author Mujuzi Moses
 */
@Service
public class CatalogService {

    public List<String> findFeaturedProducts() {
        return List.of("Laptop", "Keyboard", "Monitor");
    }
}