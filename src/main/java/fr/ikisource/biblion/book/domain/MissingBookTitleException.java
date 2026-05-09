package fr.ikisource.biblion.book.domain;

public class MissingBookTitleException extends RuntimeException {

    public MissingBookTitleException(Isbn isbn) {
        super("Cannot add book without a title (lookup failed for ISBN " + isbn.value() + ")");
    }
}
