package fr.ikisource.biblion.book.domain.api;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookId;

import java.util.Optional;

public interface GetBookById {

    Optional<Book> execute(BookId id);
}
