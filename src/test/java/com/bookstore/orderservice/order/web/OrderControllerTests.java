package com.bookstore.orderservice.order.web;

import com.bookstore.orderservice.book.BookDto;
import com.bookstore.orderservice.config.SecurityConfig;
import com.bookstore.orderservice.order.domain.Order;
import com.bookstore.orderservice.order.domain.OrderService;
import com.bookstore.orderservice.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(OrderController.class)
@Import(SecurityConfig.class)
public class OrderControllerTests {

    @MockBean
    OrderService orderService;

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void whenBookNotAvailableThenSavedRejectedOrder() {
        var orderRequest = new OrderRequest("1234567890", 3);
        var rejectedOrder = OrderService.buildRejectedOrder(orderRequest.isbn(), orderRequest.quantity());
        BDDMockito.given(orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity()))
                        .willReturn(Mono.just(rejectedOrder));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_customer")))
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

    @Test
    void whenBookAvailableThenSavedAcceptedOrder() {
        var isbn = "1234567890";
        var book = new BookDto(isbn, "Title", "Author", 19.9);
        var orderRequest = new OrderRequest(isbn, 3);
        BDDMockito.given(orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity()))
                .willReturn(Mono.just(OrderService.buildAcceptedOrder(book, 3)));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_customer")))
                .post()
                .uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class)
                .value(actualOrder -> {
                    assertThat(actualOrder.isbn()).isEqualTo(isbn);
                    assertThat(actualOrder.status()).isEqualTo(OrderStatus.ACCEPTED);
                });

    }

}
