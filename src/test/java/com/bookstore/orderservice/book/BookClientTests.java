package com.bookstore.orderservice.book;

import okhttp3.mockwebserver.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

@TestMethodOrder(MethodOrderer.Random.class)
public class BookClientTests {

    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.bookClient = new BookClient(webClient);
    }

    @AfterEach
    void clean() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void whenBookExistsThenBookReturn() {
        var isbn = "1234567890";
        var mockResponse = new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        {
                            "isbn": %s,
                            "title": "Title",
                            "author": "Author",
                            "price": 9.90
                        }
                        """.formatted(isbn));
        mockWebServer.enqueue(mockResponse);

        Mono<BookDto> book = bookClient.getBookByIsbn(isbn);
        StepVerifier.create(book)
                .expectNextMatches(b -> b.isbn().equals(isbn))
                .verifyComplete();
    }

}
