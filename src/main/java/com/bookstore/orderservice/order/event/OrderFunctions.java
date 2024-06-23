package com.bookstore.orderservice.order.event;

import com.bookstore.orderservice.order.domain.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OrderFunctions {

    private static final Logger log = LoggerFactory.getLogger(OrderFunctions.class);

    @Bean
    public Consumer<OrderDispatchedMessage> dispatchOrder(OrderService orderService) {
        return orderService::consumeOrderDispatchedEvent;
    }

}
