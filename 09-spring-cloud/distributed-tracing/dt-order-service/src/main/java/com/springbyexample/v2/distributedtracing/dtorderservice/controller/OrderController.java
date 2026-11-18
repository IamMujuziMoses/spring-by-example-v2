package com.springbyexample.v2.distributedtracing.dtorderservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.distributedtracing.dtorderservice.model.OrderRequest;
import com.springbyexample.v2.distributedtracing.dtorderservice.model.OrderResponse;
import com.springbyexample.v2.distributedtracing.dtorderservice.service.OrderService;

import jakarta.validation.Valid;

/**
 * Exposes order processing endpoints.
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
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.process(request);
    }
}

