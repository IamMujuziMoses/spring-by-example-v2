package com.springbyexample.v2.messaging.msgintegrationtests.order.contoller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.springbyexample.v2.messaging.msgorderservice.OrderServiceApplication;
import com.springbyexample.v2.messaging.msgorderservice.controller.OrderController;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderAcceptedResponse;
import com.springbyexample.v2.messaging.msgorderservice.model.OrderRequest;
import com.springbyexample.v2.messaging.msgorderservice.service.OrderService;

/**
 * Web MVC tests for {@link OrderController}.
 *
 * @author Mujuzi Moses
 */
@WebMvcTest(OrderController.class)
@ContextConfiguration(classes = OrderServiceApplication.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldAcceptOrder() throws Exception {
        given(orderService.createOrder(new OrderRequest("order-001", "SKU-1001", 2)
        )).willReturn(new OrderAcceptedResponse("order-001", "ACCEPTED"));

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "order-001",
                                  "productId": "SKU-1001",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value("order-001"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(orderService).createOrder(new OrderRequest("order-001", "SKU-1001", 2));
    }

    @Test
    void shouldRejectOrderWithBlankOrderId() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "",
                                  "productId": "SKU-1001",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectOrderWithBlankProductId() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "order-001",
                                  "productId": " ",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectOrderWithNonPositiveQuantity() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "order-001",
                                  "productId": "SKU-1001",
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }
}
