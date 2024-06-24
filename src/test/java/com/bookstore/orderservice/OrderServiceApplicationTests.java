package com.bookstore.orderservice;

import com.bookstore.orderservice.book.BookClient;
import com.bookstore.orderservice.book.BookDto;
import com.bookstore.orderservice.order.dto.LineItemRequest;
import com.bookstore.orderservice.order.dto.OrderRequest;
import com.bookstore.orderservice.order.dto.PaymentRequest;
import com.bookstore.orderservice.order.dto.UserInformation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
            DockerImageName.parse("postgres:16"));

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer(
            "quay.io/keycloak/keycloak:23.0")
            .withRealmImportFile("bookstore-realm.json");

    @Autowired
    private OutputDestination output;

    @BeforeAll
    static void setup() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloak.getAuthServerUrl() + "realms/bookstore/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        employeeToken = authenticatedWith("employee", "1", webClient);
        customerToken = authenticatedWith("user", "1", webClient);
    }

    @AfterEach
    void clean() {
        output.clear();
    }

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "realms/bookstore");
    }

    @Test
    void whenAuthenticatedPostOrderThenCreated() {
        Map.Entry<String, Integer> book1 = Map.entry("1234567891", 1);
        Map.Entry<String, Integer> book2 = Map.entry("1234567892", 2);
        OrderRequest orderRequest = buildOrderRequest(Map.of(book1.getKey(), book1.getValue(), book2.getKey(), book2.getValue()));
        // Mock
        var book1Dto = new BookDto(book1.getKey(), "Title1", "Author1", "Publisher1", "Supplier1", 1900000L, null, 1);
        var book2Dto = new BookDto(book2.getKey(), "Title2", "Author2", "Publisher2", "Supplier2", 1900000L, null, 2);

        when(bookClient.getBookByIsbn(book1.getKey())).thenReturn(ResponseEntity.ok(book1Dto));
        when(bookClient.getBookByIsbn(book2.getKey())).thenReturn(ResponseEntity.ok(book2Dto));
        webTestClient.post()
                .uri("/orders")
                .headers(headers -> headers.setBearerAuth(customerToken.accessToken))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().isCreated();
    }


    @Test
    void whenAuthenticatedGetPaymentUrlThenSuccess() {
        Map.Entry<String, Integer> book1 = Map.entry("1234567891", 1);
        Map.Entry<String, Integer> book2 = Map.entry("1234567892", 2);
        OrderRequest orderRequest = buildOrderRequest(Map.of(book1.getKey(), book1.getValue(), book2.getKey(), book2.getValue()));
        // Mock
        var book1Dto = new BookDto(book1.getKey(), "Title1", "Author1", "Publisher1", "Supplier1", 1900000L, null, 1);
        var book2Dto = new BookDto(book2.getKey(), "Title2", "Author2", "Publisher2", "Supplier2", 1900000L, null, 2);

        when(bookClient.getBookByIsbn(book1.getKey())).thenReturn(ResponseEntity.ok(book1Dto));
        when(bookClient.getBookByIsbn(book2.getKey())).thenReturn(ResponseEntity.ok(book2Dto));
        String responseBody = webTestClient.post()
                .uri("/orders")
                .headers(headers -> headers.setBearerAuth(customerToken.accessToken))
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult().getResponseBody();
        String orderIdStr = JsonPath.parse(responseBody).read("$.id");
        UUID orderId = UUID.fromString(orderIdStr);

        var paymentRequest = new PaymentRequest(orderId, "NCB");
        webTestClient.post()
                .uri("/payment/vnpay")
                .bodyValue(paymentRequest)
                .headers(headers -> headers.setBearerAuth(customerToken.accessToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(jsonValue -> {
                    DocumentContext documentContext = JsonPath.parse(jsonValue);
                    assertThat(documentContext.read("$.paymentUrl", String.class)).isNotBlank();
                });
    }

    private static OrderRequest buildOrderRequest(Map<String, Integer> map) {
        var orderRequest = new OrderRequest();
        var lineItems = new ArrayList<LineItemRequest>();
        for (Map.Entry<String, Integer> entry: map.entrySet()) {
            LineItemRequest lineItemRequest = new LineItemRequest();
            lineItemRequest.setIsbn(entry.getKey());
            lineItemRequest.setQuantity(entry.getValue());
            lineItems.add(lineItemRequest);
        }
        orderRequest.setLineItems(lineItems);
        var userInfo = new UserInformation();
        userInfo.setFullName("Nguyen Thai Nguyen");
        userInfo.setEmail("nguyennt11032004@gmail.com");
        userInfo.setPhoneNumber("0987654321");
        userInfo.setCity("Ha Noi");
        userInfo.setZipCode("100000");
        userInfo.setAddress("Ha Noi, Viet Nam");
        orderRequest.setUserInformation(userInfo);
        return orderRequest;
    }


    private static KeycloakToken authenticatedWith(String username, String password, WebClient webClient) {
        return webClient
                .post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "edge-service")
                        .with("client_secret", "cT5pq7W3XStcuFVQMhjPbRj57Iqxcu4n")
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
