package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.order.domain.Order;
import com.bookstore.orderservice.order.domain.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Flux<Order> getAllOrder() {
        log.info("OrderController attempt retrieve orders.");
        return orderService.getAllOrder("unknown");
    }

    @PostMapping
    public Mono<Order> submitOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity());
    }

}