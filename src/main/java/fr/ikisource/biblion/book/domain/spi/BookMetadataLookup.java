package fr.ikisource.biblion.book.domain.spi;

import fr.ikisource.biblion.book.domain.BookMetadata;
import fr.ikisource.biblion.book.domain.Isbn;

import java.util.Optional;

public interface BookMetadataLookup {

    Optional<BookMetadata> findByIsbn(Isbn isbn);
}
