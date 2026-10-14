package com.springbyexample.v2.applicationevents.catalog;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.springbyexample.v2.applicationevents.catalog.internal.ProductRepository;

/**
 * @author Mujuzi Moses
 */
@ExtendWith(MockitoExtension.class)
public class CatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void registerProduct_shouldReturnRegisteredProduct() {
        Product product = catalogService.registerProduct("SKU-003", "Monitor");

        assertThat(product).isEqualTo(new Product("SKU-003", "Monitor"));

        verify(productRepository).save(product);
        verify(eventPublisher).publishEvent(new ProductRegisteredEvent("SKU-003", "Monitor"));
    }

    @Test
    void findBySku_shouldFindProductBySku() {
        Product product = new Product("SKU-001", "Laptop");

        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(product));

        assertThat(catalogService.findBySku("SKU-001")).contains(product);
    }
}
