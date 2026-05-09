package fr.ikisource.biblion.book.application;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.api.ListBooks;
import fr.ikisource.biblion.book.domain.spi.BookRepository;

import java.util.List;

public class ListBooksUseCase implements ListBooks {

    private final BookRepository books;

    public ListBooksUseCase(BookRepository books) {
        this.books = books;
    }

    @Override
    public List<Book> execute() {
        return books.findAll();
    }
}
