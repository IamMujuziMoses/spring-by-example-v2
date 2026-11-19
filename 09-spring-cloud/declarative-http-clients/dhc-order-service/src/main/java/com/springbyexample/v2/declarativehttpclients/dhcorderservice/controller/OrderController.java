package com.springbyexample.v2.declarativehttpclients.dhcorderservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.OrderRequest;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.model.OrderResponse;
import com.springbyexample.v2.declarativehttpclients.dhcorderservice.service.OrderService;

import jakarta.validation.Valid;

/**
 * REST controller exposing order endpoints.
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

    /**
     * Creates and processes an order.
     *
     * @param request the order request
     * @return the order processing result
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.process(request);
    }
}
