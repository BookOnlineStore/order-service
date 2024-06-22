package com.bookstore.orderservice.book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BookClient {

    private static final String BOOK_ROOT_API = "/books/";
    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(BookClient.class);

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<BookDto> getBookByIsbn(String isbn) {
        return webClient
                .get().uri(BOOK_ROOT_API + isbn)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .bodyToMono(BookDto.class)
                .doOnNext(bookDto -> log.info("BookClient retrieve book with isbn {} from " +
                        "Catalog Service successfully.", isbn))
                .timeout(Duration.ofSeconds(3))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(throwable -> throwable instanceof WebClientResponseException.InternalServerError)
                )
                .onErrorResume(Exception.class, exception -> Mono.empty());
    }
}

