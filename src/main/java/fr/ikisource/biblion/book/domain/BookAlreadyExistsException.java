package fr.ikisource.biblion.book.domain;

public class BookAlreadyExistsException extends RuntimeException {

    private final Isbn isbn;

    public BookAlreadyExistsException(Isbn isbn) {
        super("Book already exists: " + isbn.value());
        this.isbn = isbn;
    }

    public Isbn isbn() {
        return isbn;
    }
}
