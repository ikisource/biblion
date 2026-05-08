package fr.ikisource.biblion.book.domain.spi;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookId;

import java.util.Optional;

public interface BookRepository {

    Optional<Book> findById(BookId id);
}
