package com.bookstore.orderservice.order.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<Order, UUID> {

    Flux<Order> findAllByCreatedBy(String createdBy);

}
