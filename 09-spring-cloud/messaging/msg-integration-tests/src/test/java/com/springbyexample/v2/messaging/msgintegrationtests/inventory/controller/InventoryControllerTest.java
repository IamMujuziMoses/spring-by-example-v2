package com.springbyexample.v2.messaging.msgintegrationtests.inventory.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.messaging.msginventoryservice.InventoryServiceApplication;
import com.springbyexample.v2.messaging.msginventoryservice.controller.InventoryController;
import com.springbyexample.v2.messaging.msginventoryservice.model.InventoryItem;
import com.springbyexample.v2.messaging.msginventoryservice.service.InventoryService;

/**
 * Web MVC tests for {@link InventoryController}.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(InventoryController.class)
@ContextConfiguration(classes = InventoryServiceApplication.class)
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void shouldReturnInventoryItem() throws Exception {
        given(inventoryService.findByProductId("SKU-1001"))
                .willReturn(Optional.of(new InventoryItem("SKU-1001", 25)));

        mockMvc.perform(get("/api/inventory/{productId}", "SKU-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("SKU-1001"))
                .andExpect(jsonPath("$.availableQuantity").value(25));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        given(inventoryService.findByProductId("SKU-UNKNOWN")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/{productId}", "SKU-UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}
