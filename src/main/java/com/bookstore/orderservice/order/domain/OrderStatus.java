package com.bookstore.orderservice.order.domain;

public enum OrderStatus {
    ACCEPTED,
    WAITING_FOR_PAYMENT,
    REJECTED,
    DISPATCHED
}
