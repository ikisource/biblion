package fr.ikisource.biblion.book.infrastructure;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookAlreadyExistsException;
import fr.ikisource.biblion.book.domain.BookId;
import fr.ikisource.biblion.book.domain.Isbn;
import fr.ikisource.biblion.book.domain.MissingBookTitleException;
import fr.ikisource.biblion.book.domain.api.AddBookByIsbn;
import fr.ikisource.biblion.book.domain.api.DeleteBook;
import fr.ikisource.biblion.book.domain.api.GetBookById;
import fr.ikisource.biblion.book.domain.api.ListBooks;
import fr.ikisource.biblion.book.domain.api.LookupBookByIsbn;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.util.Map;

public class BookController {

    private final GetBookById getBookById;
    private final ListBooks listBooks;
    private final AddBookByIsbn addBookByIsbn;
    private final LookupBookByIsbn lookupBookByIsbn;
    private final DeleteBook deleteBook;

    public BookController(GetBookById getBookById,
                          ListBooks listBooks,
                          AddBookByIsbn addBookByIsbn,
                          LookupBookByIsbn lookupBookByIsbn,
                          DeleteBook deleteBook) {
        this.getBookById = getBookById;
        this.listBooks = listBooks;
        this.addBookByIsbn = addBookByIsbn;
        this.lookupBookByIsbn = lookupBookByIsbn;
        this.deleteBook = deleteBook;
    }

    public void register(Javalin app) {
        app.get("/books", this::getAll);
        app.get("/books/lookup", this::lookup);
        app.get("/books/{id}", this::getById);
        app.post("/books", this::create);
        app.delete("/books/{id}", this::delete);
    }

    private void delete(Context ctx) {
        BookId id = parseId(ctx.pathParam("id"));
        deleteBook.execute(id);
        ctx.status(204);
    }

    private void lookup(Context ctx) {
        String raw = ctx.queryParam("isbn");
        if (raw == null || raw.isBlank()) {
            ctx.html("");
            return;
        }
        Isbn isbn;
        try {
            isbn = new Isbn(raw);
        } catch (IllegalArgumentException e) {
            ctx.render("book/preview.jte", Map.of("state", "invalid"));
            return;
        }
        LookupBookByIsbn.Result result = lookupBookByIsbn.execute(isbn);
        Map<String, Object> model = switch (result.state()) {
            case ALREADY_IN_LIBRARY -> Map.of("state", "already");
            case FOUND -> Map.of("state", "found", "metadata", result.metadata());
            case NOT_FOUND -> Map.of("state", "not-found");
        };
        ctx.render("book/preview.jte", model);
    }

    private void getAll(Context ctx) {
        ctx.json(listBooks.execute().stream().map(BookController::toResponse).toList());
    }

    private void getById(Context ctx) {
        BookId id = parseId(ctx.pathParam("id"));
        Book book = getBookById.execute(id)
                .orElseThrow(() -> new NotFoundResponse("Book not found"));
        ctx.json(toResponse(book));
    }

    private void create(Context ctx) {
        Isbn isbn = parseIsbn(ctx.formParam("isbn"));
        String manualTitle = trimToNull(ctx.formParam("title"));
        String manualAuthor = trimToNull(ctx.formParam("author"));
        Book book;
        try {
            book = addBookByIsbn.execute(new AddBookByIsbn.Command(isbn, manualTitle, manualAuthor));
        } catch (BookAlreadyExistsException e) {
            throw new ConflictResponse(e.getMessage());
        } catch (MissingBookTitleException e) {
            throw new BadRequestResponse(e.getMessage());
        }
        if ("true".equals(ctx.header("HX-Request"))) {
            ctx.render("book/row.jte", Map.of("book", book));
        } else {
            ctx.status(201).json(toResponse(book));
        }
    }

    private static BookId parseId(String raw) {
        try {
            return new BookId(Long.parseLong(raw));
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Invalid book id: " + raw);
        }
    }

    private static Isbn parseIsbn(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestResponse("ISBN is required");
        }
        try {
            return new Isbn(raw);
        } catch (IllegalArgumentException e) {
            throw new BadRequestResponse(e.getMessage());
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static BookResponse toResponse(Book book) {
        return new BookResponse(book.id().value(), book.isbn().value(), book.title(), book.author());
    }

    private record BookResponse(long id, String isbn, String title, String author) {}
}
