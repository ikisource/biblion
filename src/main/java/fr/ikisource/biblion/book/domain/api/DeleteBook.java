package fr.ikisource.biblion.book.domain.api;

import fr.ikisource.biblion.book.domain.BookId;

public interface DeleteBook {

    void execute(BookId id);
}
