package fr.ikisource.biblion.book.application;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookAlreadyExistsException;
import fr.ikisource.biblion.book.domain.BookMetadata;
import fr.ikisource.biblion.book.domain.MissingBookTitleException;
import fr.ikisource.biblion.book.domain.api.AddBook;
import fr.ikisource.biblion.book.domain.spi.BookMetadataLookup;
import fr.ikisource.biblion.book.domain.spi.BookRepository;

import java.util.Optional;

public class AddBookUseCase implements AddBook {

    private final BookRepository books;
    private final BookMetadataLookup lookup;

    public AddBookUseCase(BookRepository books, BookMetadataLookup lookup) {
        this.books = books;
        this.lookup = lookup;
    }

    @Override
    public Book execute(Command command) {
        Optional<BookMetadata> metadata = Optional.empty();
        if (command.isbn() != null) {
            if (books.findByIsbn(command.isbn()).isPresent()) {
                throw new BookAlreadyExistsException(command.isbn());
            }
            metadata = lookup.findByIsbn(command.isbn());
        }
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
