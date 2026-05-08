package fr.ikisource.biblion.book.application;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookId;
import fr.ikisource.biblion.book.domain.api.GetBookById;
import fr.ikisource.biblion.book.domain.spi.BookRepository;

import java.util.Optional;

public class GetBookByIdUseCase implements GetBookById {

    private final BookRepository books;

    public GetBookByIdUseCase(BookRepository books) {
        this.books = books;
    }

    @Override
    public Optional<Book> execute(BookId id) {
        return books.findById(id);
    }
}
