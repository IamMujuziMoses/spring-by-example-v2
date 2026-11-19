package com.springbyexample.v2.messaging.msgorderservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springbyexample.v2.messaging.msgorderservice.model.OrderAcceptedResponse;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderRequest;
import com.springbyexample.v2.messaging.msgorderservice.service.OrderService;

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
     * Creates an order and publishes an event for asynchronous processing.
     *
     * @param request the order request
     * @return the order acceptance response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderAcceptedResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }
}
