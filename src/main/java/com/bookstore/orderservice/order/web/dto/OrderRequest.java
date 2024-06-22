package com.bookstore.orderservice.order.web.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    @Size(min = 1, message = "Order must have at least one line item")
    private List<LineItemRequest> lineItems;
    @Valid
    private UserInformation userInformation;
}

