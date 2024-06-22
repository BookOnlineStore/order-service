package com.bookstore.orderservice.order.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.*;
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
    private Double totalPrice;
    private OrderStatus status;

    /*User information*/
    private String fullName;
    private String email;
    private String phoneNumber;
    private String city;
    private String zipCode;
    private String address;
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