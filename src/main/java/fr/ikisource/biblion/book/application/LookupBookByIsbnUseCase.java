package fr.ikisource.biblion.book.application;

import fr.ikisource.biblion.book.domain.Isbn;
import fr.ikisource.biblion.book.domain.api.LookupBookByIsbn;
import fr.ikisource.biblion.book.domain.spi.BookMetadataLookup;
import fr.ikisource.biblion.book.domain.spi.BookRepository;

public class LookupBookByIsbnUseCase implements LookupBookByIsbn {

    private final BookRepository books;
    private final BookMetadataLookup lookup;

    public LookupBookByIsbnUseCase(BookRepository books, BookMetadataLookup lookup) {
        this.books = books;
        this.lookup = lookup;
    }

    @Override
    public Result execute(Isbn isbn) {
        if (books.findByIsbn(isbn).isPresent()) {
            return Result.alreadyInLibrary();
        }
        return lookup.findByIsbn(isbn)
                .map(Result::found)
                .orElseGet(Result::notFound);
    }
}
