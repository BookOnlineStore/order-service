package com.bookstore.orderservice.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.UUID;

@RepositoryRestResource(path = "orders")
public interface OrderRepository extends CrudRepository<Order, UUID> {

    @RestResource
    Page<Order> findAllByCreatedBy(String createdBy, Pageable pageable);
}
