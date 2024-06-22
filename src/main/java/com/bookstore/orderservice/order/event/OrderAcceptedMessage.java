package com.bookstore.orderservice.order.event;

import com.bookstore.orderservice.order.domain.LineItem;
import com.bookstore.orderservice.order.web.dto.UserInformation;

import java.util.List;
import java.util.UUID;

public record OrderAcceptedMessage(
        UUID orderId,
        List<LineItem> lineItems,
        UserInformation userInformation
) {
}
