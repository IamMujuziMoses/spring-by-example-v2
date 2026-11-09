package com.springbyexample.v2.transformers.transformer;

import java.math.BigDecimal;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;

import com.springbyexample.v2.transformers.model.OrderMessage;
import com.springbyexample.v2.transformers.model.OrderSummary;

/**
 * Transforms incoming order messages into order summaries.
 *
 * @author Mujuzi Moses
 */
@MessageEndpoint
public class OrderMessageTransformer {

    @Transformer(inputChannel = "orderInputChannel", outputChannel = "orderOutputChannel")
    public OrderSummary transform(OrderMessage order) {
        String description = order.quantity() + " x " + order.product();

        BigDecimal totalPrice = order.unitPrice().multiply(BigDecimal.valueOf(order.quantity()));

        return new OrderSummary(order.orderId(), description, totalPrice);
    }

}
