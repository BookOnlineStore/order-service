package com.bookstore.orderservice.book;

public record BookDto (
    String isbn,
    String title,
    String author,
    Double price
) {
}
