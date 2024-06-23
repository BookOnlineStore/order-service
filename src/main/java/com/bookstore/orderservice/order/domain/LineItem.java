package com.bookstore.orderservice.order.domain;

import com.bookstore.orderservice.book.BookDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Table("line_items")
@Getter
@Setter
public class LineItem {

    @Id
    private Long id;
    @NotNull(message = "Book is required")
    private UUID orderId;

    /*Book DTO*/
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private BookDto bookDto;
    /*End Book DTO*/

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;
    @Version
    private int version;

    public LineItem() {
    }

}
