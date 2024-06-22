package com.bookstore.orderservice.order.domain;

public class OrderSubmittedException extends RuntimeException{
    public OrderSubmittedException() {
    }

    public OrderSubmittedException(String message) {
        super(message);
    }

}
