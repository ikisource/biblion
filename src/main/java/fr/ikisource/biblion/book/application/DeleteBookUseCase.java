package fr.ikisource.biblion.book.application;

import fr.ikisource.biblion.book.domain.BookId;
import fr.ikisource.biblion.book.domain.api.DeleteBook;
import fr.ikisource.biblion.book.domain.spi.BookRepository;

public class DeleteBookUseCase implements DeleteBook {

    private final BookRepository books;

    public DeleteBookUseCase(BookRepository books) {
        this.books = books;
    }

    @Override
    public void execute(BookId id) {
        books.delete(id);
    }
}
