package com.bookstore.orderservice.order;

import com.bookstore.orderservice.order.dto.UserInformation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("orders")
@Getter
@Setter
@NoArgsConstructor
public class Order  {
    @Id
    private UUID id;
    private Long totalPrice;
    private OrderStatus status;

    /*User information*/
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private UserInformation userInformation;
    /*End user information*/

    @CreatedDate
    private Instant createdDate;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private Instant lastModifiedDate;
    @LastModifiedBy
    private String lastModifiedBy;

    @Version
    private int version;

}