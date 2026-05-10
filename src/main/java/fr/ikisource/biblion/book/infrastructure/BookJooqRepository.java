package fr.ikisource.biblion.book.infrastructure;

import fr.ikisource.biblion.book.domain.Book;
import fr.ikisource.biblion.book.domain.BookId;
import fr.ikisource.biblion.book.domain.Isbn;
import fr.ikisource.biblion.book.domain.spi.BookRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Optional;

public class BookJooqRepository implements BookRepository {

    private final DSLContext db;

    public BookJooqRepository(DSLContext db) {
        this.db = db;
    }

    @Override
    public Optional<Book> findById(BookId id) {
        return select()
                .where(DSL.field("id", Long.class).eq(id.value()))
                .fetchOptional()
                .map(this::toBook);
    }

    @Override
    public Optional<Book> findByIsbn(Isbn isbn) {
        return select()
                .where(DSL.field("isbn", String.class).eq(isbn.value()))
                .fetchOptional()
                .map(this::toBook);
    }

    @Override
    public List<Book> findAll() {
        return select()
                .orderBy(DSL.field("title"))
                .fetch(this::toBook);
    }

    @Override
    public void delete(BookId id) {
        db.deleteFrom(DSL.table("book"))
                .where(DSL.field("id", Long.class).eq(id.value()))
                .execute();
    }

    @Override
    public Book save(Book book) {
        Long id = db.insertInto(DSL.table("book"))
                .set(DSL.field("isbn", String.class), book.isbn() != null ? book.isbn().value() : null)
                .set(DSL.field("title", String.class), book.title())
                .set(DSL.field("author", String.class), book.author())
                .set(DSL.field("cover_url", String.class), book.coverUrl())
                .returning(DSL.field("id", Long.class))
                .fetchOne()
                .get(DSL.field("id", Long.class));
        return new Book(new BookId(id), book.isbn(), book.title(), book.author(), book.coverUrl());
    }

    private org.jooq.SelectJoinStep<? extends Record> select() {
        return db.select(
                        DSL.field("id", Long.class),
                        DSL.field("isbn", String.class),
                        DSL.field("title", String.class),
                        DSL.field("author", String.class),
                        DSL.field("cover_url", String.class))
                .from(DSL.table("book"));
    }

    private Book toBook(Record record) {
        String rawIsbn = record.get("isbn", String.class);
        return new Book(
                new BookId(record.get("id", Long.class)),
                rawIsbn != null ? new Isbn(rawIsbn) : null,
                record.get("title", String.class),
                record.get("author", String.class),
                record.get("cover_url", String.class)
        );
    }
}
