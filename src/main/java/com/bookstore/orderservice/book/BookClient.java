package com.bookstore.orderservice.book;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BookClient {

    private static final String BOOK_ROOT_API = "/books/";
    private final RestTemplate restTemplate;

    public BookClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<BookDto> getBookByIsbn(String isbn) {
        return restTemplate.getForEntity(BOOK_ROOT_API + isbn, BookDto.class);
    }
}

