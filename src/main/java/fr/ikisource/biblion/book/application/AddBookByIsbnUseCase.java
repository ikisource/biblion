package fr.ikisource.biblion.book.application;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookAlreadyExistsException;
import fr.ikisource.biblion.book.domain.BookMetadata;
import fr.ikisource.biblion.book.domain.MissingBookTitleException;
import fr.ikisource.biblion.book.domain.api.AddBookByIsbn;
import fr.ikisource.biblion.book.domain.spi.BookMetadataLookup;
import fr.ikisource.biblion.book.domain.spi.BookRepository;

import java.util.Optional;

public class AddBookByIsbnUseCase implements AddBookByIsbn {

    private final BookRepository books;
    private final BookMetadataLookup lookup;

    public AddBookByIsbnUseCase(BookRepository books, BookMetadataLookup lookup) {
        this.books = books;
        this.lookup = lookup;
    }

    @Override
    public Book execute(Command command) {
        if (books.findByIsbn(command.isbn()).isPresent()) {
            throw new BookAlreadyExistsException(command.isbn());
        }
        Optional<BookMetadata> metadata = lookup.findByIsbn(command.isbn());
        String title = firstNonBlank(command.manualTitle(), metadata.map(BookMetadata::title).orElse(null));
        String author = firstNonBlank(command.manualAuthor(), metadata.map(BookMetadata::author).orElse(null));
        if (title == null) {
            throw new MissingBookTitleException(command.isbn());
        }
        return books.save(new Book(null, command.isbn(), title, author));
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
