package com.springbyexample.v2.apigateway.aginventoryservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing inventory-related endpoints.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @GetMapping("/{sku}")
    public InventoryResponse getInventory(@PathVariable String sku) {
        return new InventoryResponse("inventory-service", sku, 100);
    }

}
