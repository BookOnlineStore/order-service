package com.bookstore.orderservice.order.web.dto;

public record VNPayDto (
        int statusCode,
        String message,
        String paymentUrl
) {
}
