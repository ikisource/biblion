package fr.ikisource.biblion.book.infrastructure;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookId;
import fr.ikisource.biblion.book.domain.api.GetBookById;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public class BookController {

    private final GetBookById getBookById;

    public BookController(GetBookById getBookById) {
        this.getBookById = getBookById;
    }

    public void register(Javalin app) {
        app.get("/books/{id}", this::getById);
    }

    private void getById(Context ctx) {
        BookId id = parseId(ctx.pathParam("id"));
        Book book = getBookById.execute(id)
                .orElseThrow(() -> new NotFoundResponse("Book not found"));
        ctx.json(new BookResponse(book.id().value(), book.title(), book.author()));
    }

    private static BookId parseId(String raw) {
        try {
            return new BookId(Long.parseLong(raw));
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Invalid book id: " + raw);
        }
    }

    private record BookResponse(long id, String title, String author) {}
}
