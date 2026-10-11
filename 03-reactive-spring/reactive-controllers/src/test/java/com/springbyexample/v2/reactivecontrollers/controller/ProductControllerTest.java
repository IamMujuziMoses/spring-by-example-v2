package com.springbyexample.v2.reactivecontrollers.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springbyexample.v2.reactivecontrollers.service.ProductService;

/**
 * @author Mujuzi Moses
 */
@WebFluxTest(ProductController.class)
@Import(ProductService.class)
public class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void findAll_shouldReturnAllProducts() {
        // The collection endpoint should serialize the Flux as a JSON array.
        webTestClient.get()
                .uri("/api/products").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].name").isEqualTo("Laptop")
                .jsonPath("$[1].name").isEqualTo("Keyboard")
                .jsonPath("$[2].name").isEqualTo("Notebook");
    }

    @Test
    void findAll_shouldFilterProductsByCategory() {
        // The optional query parameter selects the matching products.
        webTestClient.get()
                .uri("/api/products?category=Electronics").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Laptop")
                .jsonPath("$[1].name").isEqualTo("Keyboard");
    }

    @Test
    void findById_shouldReturnProductWhenItExists() {
        // A matching Mono should be mapped to an HTTP 200 response.
        webTestClient.get()
                .uri("/api/products/1").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Laptop")
                .jsonPath("$.category").isEqualTo("Electronics");
    }

    @Test
    void findById_shouldReturnNotFoundWhenProductDoesNotExist() {
        // An empty Mono triggers defaultIfEmpty and becomes HTTP 404.
        webTestClient.get()
                .uri("/api/products/999").exchange().expectStatus().isNotFound().expectBody()
                .isEmpty();
    }

    @Test
    void findAll_shouldReturnEmptyArrayWhenCategoryHasNoMatches() {
        // Filtering to zero matches produces an empty Flux, not a 404.
        webTestClient.get()
                .uri("/api/products?category=Furniture").exchange().expectStatus().isOk().expectBody()
                .json("[]");
    }

    @Test
    void findAll_shouldReturnAllProductsWhenCategoryIsBlank() {
        // A blank category should be treated like an absent category.
        webTestClient.get()
                .uri("/api/products?category=").exchange().expectStatus().isOk().expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].name").isEqualTo("Laptop")
                .jsonPath("$[1].name").isEqualTo("Keyboard")
                .jsonPath("$[2].name").isEqualTo("Notebook");
    }
}
