package fr.ikisource.biblion.book.domain;

public class MissingBookTitleException extends RuntimeException {

    public MissingBookTitleException(Isbn isbn) {
        super(isbn != null
                ? "Cannot add book without a title (lookup failed for ISBN " + isbn.value() + ")"
                : "Cannot add book without a title");
    }
}
