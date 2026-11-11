package com.springbyexample.v2.gateways.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.gateways.model.OrderMessage;
import com.springbyexample.v2.gateways.model.OrderProcessingResult;

/**
 * Service for processing incoming orders.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderMessageService {

    public OrderProcessingResult process(OrderMessage order, String source) {
        BigDecimal totalPrice = order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));

        return new OrderProcessingResult(order.orderId(), order.product(), order.quantity(), totalPrice, source);
    }
}
