package com.springbyexample.v2.serviceactivators.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.springbyexample.v2.serviceactivators.model.OrderMessage;
import com.springbyexample.v2.serviceactivators.model.OrderProcessingResult;

/**
 * Provides order processing business logic.
 *
 * @author Mujuzi Moses
 */
@Service
public class OrderMessageService {

    public OrderProcessingResult process(OrderMessage order) {
        BigDecimal totalPrice = order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));

        return new OrderProcessingResult(order.orderId(), order.product(), order.quantity(), totalPrice);
    }
}
