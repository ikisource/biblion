package fr.ikisource.biblion.book.domain;

public record Book(BookId id, Isbn isbn, String title, String author, String coverUrl) {
}
