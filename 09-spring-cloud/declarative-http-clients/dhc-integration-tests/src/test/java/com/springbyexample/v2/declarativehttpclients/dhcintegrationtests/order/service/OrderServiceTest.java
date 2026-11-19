package com.springbyexample.v2.declarativehttpclients.dhcintegrationtests.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springbyexample.v2.declarativehttpclients.dhcorderservice.client.InventoryClient;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.InventoryResponse;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.OrderRequest;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.OrderResponse;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.service.OrderService;

/**
 * Unit tests for OrderService.
 *
 * @author Mujuzi Moses
 */
public class OrderServiceTest {

    private static final String PRODUCT_ID = "SKU-1001";

    private InventoryClient inventoryClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        inventoryClient = mock(InventoryClient.class);
        orderService = new OrderService(inventoryClient);
    }

    @Test
    void shouldAcceptOrderWhenInventoryIsSufficient() {
        when(inventoryClient.getInventory(PRODUCT_ID)).thenReturn(new InventoryResponse(PRODUCT_ID, 25));

        OrderResponse response = orderService.process(new OrderRequest("ORD-1001", PRODUCT_ID, 2));

        assertThat(response.orderId()).isEqualTo("ORD-1001");
        assertThat(response.productId()).isEqualTo(PRODUCT_ID);
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.availableQuantity()).isEqualTo(25);
        assertThat(response.status()).isEqualTo("ACCEPTED");
    }

    @Test
    void shouldRejectOrderWhenInventoryIsInsufficient() {
        when(inventoryClient.getInventory(PRODUCT_ID)).thenReturn(new InventoryResponse(PRODUCT_ID, 1));

        OrderResponse response = orderService.process(new OrderRequest("ORD-1002", PRODUCT_ID, 2));

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.availableQuantity()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenInventoryResponseIsNull() {
        when(inventoryClient.getInventory(PRODUCT_ID)).thenReturn(null);

        assertThatThrownBy(() -> orderService.process(new OrderRequest("ORD-1003", PRODUCT_ID, 2)
        )).isInstanceOf(IllegalStateException.class).hasMessage("Inventory Service returned an empty response");
    }

    @Test
    void shouldPropagateInventoryServiceFailure() {
        when(inventoryClient.getInventory(PRODUCT_ID)).thenThrow(new IllegalStateException("Inventory Service unavailable"));

        assertThatThrownBy(() -> orderService.process(new OrderRequest("ORD-1004", PRODUCT_ID, 2)
        )).isInstanceOf(IllegalStateException.class).hasMessage("Inventory Service unavailable");
    }

    @Test
    void shouldInvokeInventoryClientWithCorrectProductId() {
        when(inventoryClient.getInventory(PRODUCT_ID)).thenReturn(new InventoryResponse(PRODUCT_ID, 25));

        orderService.process(new OrderRequest("ORD-1005", PRODUCT_ID, 2));

        verify(inventoryClient).getInventory(PRODUCT_ID);
    }
}
