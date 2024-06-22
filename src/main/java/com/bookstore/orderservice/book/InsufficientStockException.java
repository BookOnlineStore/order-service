package com.bookstore.orderservice.book;

public class InsufficientStockException extends RuntimeException{

    public InsufficientStockException() {
    }

    public InsufficientStockException(String message) {
        super(message);
    }
}
