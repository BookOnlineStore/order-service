package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.order.domain.Order;
import com.bookstore.orderservice.order.domain.OrderService;
import com.bookstore.orderservice.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(OrderController.class)
public class OrderControllerTests {

    @MockBean
    OrderService orderService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void whenBookNotAvailableThenSavedRejectedOrder() {
        var orderRequest = new OrderRequest("1234567890", 3);
        var rejectedOrder = OrderService.buildRejectedOrder(orderRequest.isbn(), orderRequest.quantity());
        BDDMockito.given(orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity()))
                        .willReturn(Mono.just(rejectedOrder));

        webTestClient
                .post().uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class)
                .value(actualOrder -> {
                    assertThat(actualOrder).isNotNull();
                    assertThat(actualOrder.status()).isEqualTo(OrderStatus.REJECTED);
                });
    }

}
