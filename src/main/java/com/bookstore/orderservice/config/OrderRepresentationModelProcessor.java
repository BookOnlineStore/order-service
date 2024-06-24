package com.bookstore.orderservice.config;

import com.bookstore.orderservice.order.Order;
import com.bookstore.orderservice.order.dto.PaymentRequest;
import com.bookstore.orderservice.order.web.OrderController;
import com.bookstore.orderservice.order.web.VnpayController;
import lombok.SneakyThrows;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * The type Order representation model processor.
 */
@Component
public class OrderRepresentationModelProcessor
        implements RepresentationModelProcessor<EntityModel<Order>> {

    @SneakyThrows
    @Override
    public EntityModel<Order> process(EntityModel<Order> model) {
        model.add(
                linkTo(methodOn(OrderController.class).submitOrder(null))
                        .withRel("create-order"),
                linkTo(methodOn(VnpayController.class).pay(null, new PaymentRequest(null, null)))
                        .withRel("generate paymentUrl")
        );
        return model;
    }
}
