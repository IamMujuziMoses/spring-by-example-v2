package com.springbyexample.v2.applicationevents.inventory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * @author Mujuzi Moses
 */
@Service
public class InventoryService {

    private final Set<String> registeredProductSkus = ConcurrentHashMap.newKeySet();

    public void recordRegisteredProduct(String sku) {
        registeredProductSkus.add(sku);
    }

    public boolean containsSku(String sku) {
        return registeredProductSkus.contains(sku);
    }
}

