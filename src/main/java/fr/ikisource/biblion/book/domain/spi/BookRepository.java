package fr.ikisource.biblion.book.domain.spi;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookId;
import fr.ikisource.biblion.book.domain.Isbn;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Optional<Book> findById(BookId id);

    Optional<Book> findByIsbn(Isbn isbn);

    List<Book> findAll();

    Book save(Book book);

    void delete(BookId id);
}
