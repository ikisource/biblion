package fr.ikisource.biblion.book.domain.api;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.Isbn;

public interface AddBookByIsbn {

    Book execute(Command command);

    record Command(Isbn isbn, String manualTitle, String manualAuthor) {}
}
