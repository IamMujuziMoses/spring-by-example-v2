package com.springbyexample.v2.integrationbetweenspringtechnologies.ordering;

import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.Product;
import com.springbyexample.v2.integrationbetweenspringtechnologies.catalog.ProductService;
import com.springbyexample.v2.integrationbetweenspringtechnologies.inventory.InventoryService;

/**
 * Coordinates order placement and retrieval.
 *
 * @author Mujuzi Moses
 */
@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductService productService;

    private final InventoryService inventoryService;

    private final ApplicationEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository, ProductService productService, InventoryService inventoryService,
            ApplicationEventPublisher eventPublisher) {

        this.orderRepository = orderRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Order placeOrder(String customerUsername, Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero");
        }

        Product product = productService.getProduct(productId);

        inventoryService.reserveStock(productId, quantity);

        OrderItem item = new OrderItem(product.getId(), product.getName(), quantity, product.getPrice());

        Order order = orderRepository.save(new Order(customerUsername, List.of(item)));

        eventPublisher.publishEvent(new OrderPlacedEvent(order.getId(), order.getCustomerUsername(), order.getTotalAmount(),
                Instant.now()));

        return order;
    }

    public List<Order> getOrders(String customerUsername) {

        return orderRepository.findAllByCustomerUsernameOrderByCreatedAtDesc(customerUsername);
    }

    public Order getOrder(Long orderId, String customerUsername) {

        return orderRepository.findById(orderId).filter(order -> order.getCustomerUsername()
                        .equals(customerUsername)).orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
