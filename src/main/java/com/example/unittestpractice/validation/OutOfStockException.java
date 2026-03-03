package com.example.unittestpractice.validation;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String bookId) {
        super("Book with id " + bookId + " is out of stock");
    }
}
