package com.bookstore.orderservice.order.event;

import java.util.UUID;

public record OrderDispatchedMessage (
        UUID orderId
) {
}
