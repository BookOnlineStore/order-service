package com.bookstore.orderservice.order.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface LineItemRepository extends ReactiveCrudRepository<LineItem, Long> {

    Flux<LineItem> findAllByOrderId(UUID orderId);

}
