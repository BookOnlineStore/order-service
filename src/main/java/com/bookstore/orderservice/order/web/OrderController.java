package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.order.Order;
import com.bookstore.orderservice.order.OrderRepository;
import com.bookstore.orderservice.order.OrderService;
import com.bookstore.orderservice.order.dto.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public List<Order> getAllOrder(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        log.info("OrderController attempt retrieve orders.");
        String username = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME).toString();
        return orderRepository.findAllByCreatedBy(username, pageable).getContent();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order submitOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return orderService.submitOrder(orderRequest.getLineItems(), orderRequest.getUserInformation());
    }

}
