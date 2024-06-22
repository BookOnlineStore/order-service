package com.bookstore.orderservice.order.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Table("line_items")
@Getter
@Setter
public class LineItem {

    @Id
    private Long id;
    private UUID orderId;
    @NotNull(message = "Book is required")

    /*Book DTO*/
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String supplier;
    private Double price;
    private List<String> photos;
    /*End Book DTO*/

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;
    @Version
    private int version;

    public LineItem() {
    }

}
