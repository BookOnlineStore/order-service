package com.bookstore.orderservice.order.dto;

public record PaymentUrlDto(
        int statusCode,
        String message,
        String paymentUrl
) {
}
