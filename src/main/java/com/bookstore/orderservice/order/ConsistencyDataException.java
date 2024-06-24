package com.bookstore.orderservice.order;

public class ConsistencyDataException extends RuntimeException{

    public ConsistencyDataException(String cause) {
        super("Consistency data exception: " + cause);
    }

}
