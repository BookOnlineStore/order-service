package com.bookstore.orderservice.order.domain;

import com.bookstore.orderservice.book.BookClient;
import com.bookstore.orderservice.book.BookDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookClient bookClient;

    public OrderService(OrderRepository orderRepository, BookClient bookClient) {
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
    }

    public Flux<Order> getAllOrder(String username) {
        return orderRepository.findAllByCreatedBy(username);
    }

    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn)
                .map(book -> buildAcceptedOrder(book, quantity))
                .defaultIfEmpty(buildRejectedOrder(isbn, quantity))
                .flatMap(orderRepository::save);
    }

    public static Order buildRejectedOrder(String isbn, int quantity) {
        return Order.of(isbn, null,
                null, quantity, OrderStatus.REJECTED);
    }

    public static Order buildAcceptedOrder(BookDto book, int quantity) {
        return Order.of(book.isbn(), book.title(),
                book.price(), quantity, OrderStatus.ACCEPTED);
    }
}
