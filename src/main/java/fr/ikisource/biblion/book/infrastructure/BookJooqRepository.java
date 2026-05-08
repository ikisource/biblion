package fr.ikisource.biblion.book.infrastructure;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookId;
import fr.ikisource.biblion.book.domain.spi.BookRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.util.Optional;

public class BookJooqRepository implements BookRepository {

    private final DSLContext db;

    public BookJooqRepository(DSLContext db) {
        this.db = db;
    }

    @Override
    public Optional<Book> findById(BookId id) {
        return db.select(
                        DSL.field("id", Long.class),
                        DSL.field("title", String.class),
                        DSL.field("author", String.class))
                .from(DSL.table("book"))
                .where(DSL.field("id", Long.class).eq(id.value()))
                .fetchOptional()
                .map(this::toBook);
    }

    private Book toBook(Record record) {
        return new Book(
                new BookId(record.get("id", Long.class)),
                record.get("title", String.class),
                record.get("author", String.class)
        );
    }
}
