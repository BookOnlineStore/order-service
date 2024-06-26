package com.bookstore.orderservice.order.event;

import java.util.Map;
import java.util.UUID;

public record BooksPlacedMessage (
        UUID orderId,
        Map<String, Integer> lineItems
){

}
