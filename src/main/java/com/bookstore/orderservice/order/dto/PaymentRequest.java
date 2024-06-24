package com.bookstore.orderservice.order.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

public record PaymentRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,
        @NotNull(message = "Price is required")
        @Min(value = 1000, message = "Price must be greater than or equal to 1000")
        Long price,
        @NotBlank(message = "Bank code is required")
        String bankCode
) {
}
