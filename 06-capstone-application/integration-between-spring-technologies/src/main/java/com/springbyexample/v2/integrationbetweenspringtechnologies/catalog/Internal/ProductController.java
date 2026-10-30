package com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.Internal;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.Product;
import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.ProductService;

/**
 * REST endpoints for browsing catalog products.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getProducts() {
        return productService.getProducts();
    }

    @GetMapping("/{productId}")
    public Product getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }
}
