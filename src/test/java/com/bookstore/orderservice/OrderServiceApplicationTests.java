package com.bookstore.orderservice;

import com.bookstore.orderservice.book.BookClient;
import com.bookstore.orderservice.book.BookDto;
import com.bookstore.orderservice.order.domain.Order;
import com.bookstore.orderservice.order.domain.OrderStatus;
import com.bookstore.orderservice.order.event.OrderAcceptedMessage;
import com.bookstore.orderservice.order.web.OrderRequest;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestChannelBinderConfiguration.class)
public class OrderServiceApplicationTests {

    private static KeycloakToken employeeToken;
    private static KeycloakToken customerToken;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    BookClient bookClient;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:14.10"));

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer(
            "quay.io/keycloak/keycloak:23.0")
            .withRealmImportFile("test-realm-config.json");
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    private OutputDestination output;

    @BeforeAll
    static void setup() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloak.getAuthServerUrl() + "realms/BookOnlineStore/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        employeeToken = authenticatedWith("employee", "password", webClient);
        customerToken = authenticatedWith("customer", "password", webClient);
    }

    @AfterEach
    void clean() {
        output.clear();
    }

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", OrderServiceApplicationTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "realms/BookOnlineStore");
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s", postgres.getHost(),
                postgres.getMappedPort(5432), postgres.getDatabaseName());
    }

    @Test
    void whenGetOwnOrdersThenReturn() throws IOException {
        String isbn = "1234567890";
        var bookDto = new BookDto(isbn, "Title", "Author", 9.90);
        given(bookClient.getBookByIsbn(isbn)).willReturn(Mono.just(bookDto));
        OrderRequest orderRequest = new OrderRequest(isbn, 3);

        Order expectedOrder = webTestClient
                .post()
                .uri("/orders")
                .headers(headers -> headers.setBearerAuth(customerToken.accessToken()))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Order.class).returnResult().getResponseBody();
        assertThat(expectedOrder).isNotNull();
        assertThat(jacksonObjectMapper.readValue(output.receive().getPayload(), OrderAcceptedMessage.class))
                .isEqualTo(new OrderAcceptedMessage(expectedOrder.id()));

        webTestClient
                .get()
                .uri("/orders")
                .headers(headers -> headers.setBearerAuth(customerToken.accessToken()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Order.class).value(orders -> {
                    List<Long> orderIds = orders.stream()
                            .map(Order::id).toList();
                    assertThat(orderIds).contains(expectedOrder.id());
                });
    }

    @Test
    void whenGetOrdersForAnotherUserThenNotReturned() throws IOException {
        String isbn = "1234567891";
        var bookDto = new BookDto(isbn, "Title", "Author", 9.9);
        given(bookClient.getBookByIsbn(isbn)).willReturn(Mono.just(bookDto));
        var orderRequest = new OrderRequest(isbn, 3);

        var orderByCustomer = webTestClient
                .post()
                .uri("/orders")
                .headers(headers -> headers.setBearerAuth(customerToken.accessToken()))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class).returnResult().getResponseBody();
        assertThat(orderByCustomer).isNotNull();
        assertThat(jacksonObjectMapper.readValue(output.receive().getPayload(), OrderAcceptedMessage.class))
                .isEqualTo(new OrderAcceptedMessage(orderByCustomer.id()));

        var orderByEmployee = webTestClient
                .post()
                .uri("/orders")
                .headers(headers -> headers.setBearerAuth(employeeToken.accessToken()))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class).returnResult().getResponseBody();
        assertThat(orderByEmployee).isNotNull();
        assertThat(jacksonObjectMapper.readValue(output.receive().getPayload(), OrderAcceptedMessage.class))
                .isEqualTo(new OrderAcceptedMessage(orderByEmployee.id()));

        webTestClient
                .get()
                .uri("/orders")
                .headers(headers -> headers.setBearerAuth(employeeToken.accessToken()))
                .exchange()
                .expectBodyList(Order.class)
                .value(orders -> {
                    List<Long> orderIds = orders.stream()
                            .map(Order::id)
                            .collect(Collectors.toList());
                    assertThat(orderIds).contains(orderByEmployee.id());
                    assertThat(orderIds).doesNotContain(orderByCustomer.id());
                });
    }

    @Test
    void whenPostRequestAndBookAvailableThenAcceptedOrder() {
        String isbn = "1234567892";
        var book = new BookDto(isbn, "Title", "Author", 90.90);
        given(bookClient.getBookByIsbn(isbn)).willReturn(Mono.just(book));
        var orderRequest = new OrderRequest(isbn, 2);

        webTestClient
                .post().uri("/orders")
                .headers(headers -> headers.setBearerAuth(customerToken.accessToken()))
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
        String isbn = "1234567893";
        given(bookClient.getBookByIsbn(isbn)).willReturn(Mono.empty());
        var orderRequest = new OrderRequest(isbn, 3);

        webTestClient
                .post().uri("/orders")
                .headers(headers -> headers.setBearerAuth(customerToken.accessToken()))
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

    private static KeycloakToken authenticatedWith(String username, String password, WebClient webClient) {
        return webClient
                .post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "bookstore-test")
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();
    }

    private record KeycloakToken(String accessToken) {
        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") String accessToken) {
            this.accessToken = accessToken;
        }
    }

}
