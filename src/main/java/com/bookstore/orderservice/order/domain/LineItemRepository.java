package com.bookstore.orderservice.order.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface LineItemRepository extends CrudRepository<LineItem, Long> {

    List<LineItem> findAllByOrderId(UUID orderId);

}
