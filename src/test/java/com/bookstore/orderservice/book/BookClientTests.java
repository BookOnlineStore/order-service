package com.bookstore.orderservice.book;

import okhttp3.mockwebserver.*;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.Random.class)
public class BookClientTests {

    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        var restTemplate = new RestTemplateBuilder()
                .rootUri(mockWebServer.url("/").uri().toString())
                .build();
        this.bookClient = new BookClient(restTemplate);
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

        ResponseEntity<BookDto> responseEntity = bookClient.getBookByIsbn(isbn);
        assertThat(responseEntity.getBody().isbn()).isEqualTo(isbn);
    }

    @Test
    void whenBookDoesNotExistsThenReturnEmpty() {
        var isbn = "1234567891";
        var mockResponse = new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(404);
        mockWebServer.enqueue(mockResponse);

        Assert.assertThrows(HttpClientErrorException.NotFound.class, () -> {
            bookClient.getBookByIsbn(isbn);
        });
    }
}
