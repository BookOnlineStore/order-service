package com.bookstore.orderservice.book;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class BookClient {

    private static final String BOOK_ROOT_API = "/books/";
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<BookDto> getBookByIsbn(String isbn) {
        return webClient
                .get().uri(BOOK_ROOT_API)
                .retrieve()
                .bodyToMono(BookDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty());
    }
}

