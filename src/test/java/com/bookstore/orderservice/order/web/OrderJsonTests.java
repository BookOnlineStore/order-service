package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.order.domain.Order;
import com.bookstore.orderservice.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class OrderJsonTests {

    @Autowired
    private JacksonTester<Order> json;

    @Test
    void testSerialize() throws IOException {
        var order = new Order(2004L, "1234567890", "Title",
                9.90, 3, OrderStatus.REJECTED, Instant.now(), "unknown",
                Instant.now(), "unknown", 0);
        var jsonContent = json.write(order);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id")
                .isEqualTo(order.id().intValue());
        assertThat(jsonContent).extractingJsonPathStringValue("$.isbn")
                .isEqualTo(order.isbn());
        assertThat(jsonContent).extractingJsonPathStringValue("$.bookTitle")
                .isEqualTo(order.bookTitle());
        assertThat(jsonContent).extractingJsonPathNumberValue("$.price")
                .isEqualTo(order.price());
        assertThat(jsonContent).extractingJsonPathNumberValue("$.quantity")
                .isEqualTo(order.quantity());
        assertThat(jsonContent).extractingJsonPathStringValue("$.status")
                .isEqualTo(order.status().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("$.createdDate")
                .isEqualTo(order.createdDate().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("$.createdBy")
                .isEqualTo(order.createdBy());
        assertThat(jsonContent).extractingJsonPathStringValue("$.lastModifiedDate")
                .isEqualTo(order.lastModifiedDate().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("$.lastModifiedBy")
                .isEqualTo(order.lastModifiedBy());
        assertThat(jsonContent).extractingJsonPathNumberValue("$.version")
                .isEqualTo(order.version());
    }

}
