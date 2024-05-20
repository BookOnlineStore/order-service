package com.bookstore.orderservice.order.domain;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("orders")
public record Order (
    @Id
    Long id,
    String isbn,
    String bookTitle,
    Double price,
    Integer quantity,
    OrderStatus orderStatus,

    @CreatedDate
    Instant createdDate,
    @CreatedBy
    String createdBy,
    @LastModifiedDate
    Instant lastModifiedDate,
    @LastModifiedBy
    String lastModifiedBy,

    @Version
    int version
) {
    public static Order of(String isbn, String bookTitle, Double price,
                           Integer quantity, OrderStatus status) {
        return new Order(null, isbn, bookTitle, price, quantity, status,
                null, null, null, null, 0);
    }
}
