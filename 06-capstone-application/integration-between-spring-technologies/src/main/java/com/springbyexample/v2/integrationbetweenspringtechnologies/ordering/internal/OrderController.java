package com.springbyexample.v2.integrationbetweenspringtechnologies.ordering.internal;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.integrationbetweenspringtechnologies.ordering.Order;
import com.springbyexample.v2.integrationbetweenspringtechnologies.ordering.OrderService;
import com.springbyexample.v2.integrationbetweenspringtechnologies.ordering.PlaceOrderRequest;

import jakarta.validation.Valid;

/**
 * REST endpoints for placing and retrieving customer orders.
 *
 * @author Mujuzi Moses
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order placeOrder(@Valid @RequestBody PlaceOrderRequest request, Authentication authentication) {

        return orderService.placeOrder(authentication.getName(), request.productId(), request.quantity());
    }

    @GetMapping
    public List<Order> getOrders(Authentication authentication) {
        return orderService.getOrders(authentication.getName());
    }

    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId, Authentication authentication) {

        return orderService.getOrder(orderId, authentication.getName());
    }
}
