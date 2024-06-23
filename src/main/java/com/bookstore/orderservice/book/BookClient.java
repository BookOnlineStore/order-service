package com.bookstore.orderservice.book;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class BookClient {

    private static final String BOOK_ROOT_API = "/books/";
    private static final String BOOK_REDUCE_INVENTORY_API = "/books/reduce-inventory";
    private final RestTemplate restTemplate;

    public BookClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<BookDto> getBookByIsbn(String isbn) {
        return restTemplate.getForEntity(BOOK_ROOT_API + isbn, BookDto.class);
    }

    public ResponseEntity<Void> reduceInventoryByIsbn(String isbn, Integer quantity) {
        return restTemplate
                .postForEntity(BOOK_REDUCE_INVENTORY_API
                        , Map.of("isbn", isbn, "quantity", quantity)
                        , Void.class);
    }
}

