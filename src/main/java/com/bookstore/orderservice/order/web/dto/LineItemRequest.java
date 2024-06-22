package com.bookstore.orderservice.order.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LineItemRequest {

    private String isbn;
    private Integer quantity;

}
