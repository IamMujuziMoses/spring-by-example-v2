package com.springbyexample.v2.integrationbetweenspringtechnologies;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.integrationbetweenspringtechnologies.audit.AuditRepository;
import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.Product;
import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.ProductRepository;
import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InsufficientStockException;
import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InventoryItem;
import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InventoryRepository;
import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InventoryService;
import com.springbyexample.v2.integrationbetweenspringtechnologies.ordering.OrderRepository;
import com.springbyexample.v2.integrationbetweenspringtechnologies.ordering.OrderService;

/**
 * Integration tests for the Order Management application.
 *
 * @author Mujuzi Moses
 */
@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private OrderService orderService;

    private Product product;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        auditRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();

        product = productRepository.saveAndFlush(new Product("Test Laptop", new BigDecimal("100.00")));

        inventoryRepository.saveAndFlush(new InventoryItem(product.getId(), 5));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldAllowPublicAccessToProducts() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Laptop"));
    }

    @Test
    void shouldRejectUnauthenticatedOrderRequests() throws Exception {

        mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPlaceOrderAndUpdateInventory() throws Exception {
        mockMvc.perform(post("/api/orders").with(httpBasic("customer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "productId": %d,
                                    "quantity": 2
                                }
                                """.formatted(product.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerUsername").value("customer"))
                .andExpect(jsonPath("$.totalAmount").value(200.00))
                .andExpect(jsonPath("$.status").value("PLACED"));

        assertEquals(3, inventoryService.getAvailableQuantity(product.getId()));
        assertEquals(1, orderRepository.count());
        assertEquals(1, auditRepository.count());
    }

    @Test
    void shouldReturnConflictWhenStockIsInsufficient() throws Exception {
        mockMvc.perform(post("/api/orders").with(httpBasic("customer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "productId": %d,
                                    "quantity": 10
                                }
                                """.formatted(product.getId())))
                .andExpect(status().isConflict());

        assertEquals(5, inventoryService.getAvailableQuantity(product.getId()));
        assertEquals(0, orderRepository.count());
        assertEquals(0, auditRepository.count());
    }

    @Test
    void shouldRollbackInventoryReservationWhenOrderPlacementFails() {
        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder("customer",
                product.getId(), 10));

        assertEquals(5, inventoryService.getAvailableQuantity(product.getId()));
        assertEquals(0, orderRepository.count());
        assertEquals(0, auditRepository.count());
    }

    @Test
    void shouldPreventCustomerFromAccessingAnotherCustomersOrder() throws Exception {

        var order = orderService.placeOrder("another-customer", product.getId(), 1);

        mockMvc.perform(get("/api/orders/{orderId}", order.getId())
                        .with(httpBasic("customer", "password"))).andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowCustomerToRetrieveTheirOrders() throws Exception {
        orderService.placeOrder("customer", product.getId(), 1);

        mockMvc.perform(get("/api/orders").with(httpBasic("customer", "password")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].customerUsername")
                        .value("customer"));
    }

    @Test
    void shouldAllowAdminToAccessInventoryButForbidCustomer() throws Exception {
        mockMvc.perform(get("/api/inventory/" + product.getId()).with(httpBasic("admin", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product.getId()))
                .andExpect(jsonPath("$.availableQuantity").value(5));

        mockMvc.perform(get("/api/inventory/" + product.getId())
                        .with(httpBasic("customer", "password"))).andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectOrderWithInvalidQuantity() throws Exception {
        mockMvc.perform(post("/api/orders").with(httpBasic("customer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "productId": %d,
                                "quantity": 0
                            }
                            """.formatted(product.getId())))
                .andExpect(status().isBadRequest());

        assertThat(inventoryRepository.findByProductId(product.getId())
                .orElseThrow().getAvailableQuantity()).isEqualTo(5);
        assertThat(orderRepository.count()).isZero();
        assertThat(auditRepository.count()).isZero();
    }

    @Test
    void shouldReturnNotFoundWhenOrderingNonexistentProduct() throws Exception {
        mockMvc.perform(post("/api/orders").with(httpBasic("customer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "productId": 99999,
                                "quantity": 1
                            }
                            """))
                .andExpect(status().isNotFound());

        assertThat(inventoryRepository.findByProductId(product.getId())
                .orElseThrow().getAvailableQuantity()).isEqualTo(5);
        assertThat(orderRepository.count()).isZero();
        assertThat(auditRepository.count()).isZero();
    }
}
