package com.bookstore.orderservice;

import com.bookstore.orderservice.book.BookClient;
import com.bookstore.orderservice.book.BookDto;
import com.bookstore.orderservice.order.domain.Order;
import com.bookstore.orderservice.order.domain.OrderStatus;
import com.bookstore.orderservice.order.web.OrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class OrderServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    BookClient bookClient;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:14.10"));

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", OrderServiceApplicationTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s", postgres.getHost(),
                postgres.getMappedPort(5432), postgres.getDatabaseName());
    }


    @Test
    void whenPostRequestAndBookAvailableThenAcceptedOrder() {
        String isbn = "1234567890";
        var book = new BookDto(isbn, "Title", "Author", 90.90);
        given(bookClient.getBookByIsbn(isbn)).willReturn(Mono.just(book));
        var orderRequest = new OrderRequest(isbn, 2);

        webTestClient
                .post().uri("orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class)
                .value(actualOrder -> {
                    assertThat(actualOrder.isbn()).isEqualTo(orderRequest.isbn());
                    assertThat(actualOrder.bookTitle()).isEqualTo(book.title());
                    assertThat(actualOrder.price()).isEqualTo(book.price());
                    assertThat(actualOrder.status()).isEqualTo(OrderStatus.ACCEPTED);
                });
    }

    @Test
    void whenPostRequestAndBookUnavailableThenRejectedOrder() {
        String isbn = "1234567891";
        given(bookClient.getBookByIsbn(isbn)).willReturn(Mono.empty());
        var orderRequest = new OrderRequest(isbn, 3);

        webTestClient
                .post().uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class)
                .value(actualOrder -> {
                    assertThat(actualOrder.isbn()).isEqualTo(orderRequest.isbn());
                    assertThat(actualOrder.quantity()).isEqualTo(orderRequest.quantity());
                    assertThat(actualOrder.bookTitle()).isNull();
                    assertThat(actualOrder.status()).isEqualTo(OrderStatus.REJECTED);
                });
    }

}
