package fr.ikisource.biblion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.ikisource.biblion.book.application.AddBookUseCase;
import fr.ikisource.biblion.book.application.DeleteBookUseCase;
import fr.ikisource.biblion.book.application.GetBookByIdUseCase;
import fr.ikisource.biblion.book.application.ListBooksUseCase;
import fr.ikisource.biblion.book.application.LookupBookByIsbnUseCase;
import fr.ikisource.biblion.book.domain.api.AddBook;
import fr.ikisource.biblion.book.domain.api.DeleteBook;
import fr.ikisource.biblion.book.domain.api.GetBookById;
import fr.ikisource.biblion.book.domain.api.ListBooks;
import fr.ikisource.biblion.book.domain.api.LookupBookByIsbn;
import fr.ikisource.biblion.book.domain.spi.BookMetadataLookup;
import fr.ikisource.biblion.book.domain.spi.BookRepository;
import fr.ikisource.biblion.book.infrastructure.BookController;
import fr.ikisource.biblion.book.infrastructure.BookJooqRepository;
import fr.ikisource.biblion.book.infrastructure.GoogleBooksMetadataLookup;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

public class Application {

    public static void main(String[] args) {
        DataSource dataSource = createDataSource();
        runMigrations(dataSource);

        DSLContext db = DSL.using(dataSource, SQLDialect.POSTGRES);

        TemplateEngine engine = TemplateEngine.create(
                new DirectoryCodeResolver(Path.of("src/main/jte")),
                ContentType.Html
        );

        var app = Javalin.create(config -> {
            config.fileRenderer(new JavalinJte(engine));
        });

        BookRepository bookRepository = new BookJooqRepository(db);
        BookMetadataLookup metadataLookup = new GoogleBooksMetadataLookup();
        GetBookById getBookById = new GetBookByIdUseCase(bookRepository);
        ListBooks listBooks = new ListBooksUseCase(bookRepository);
        AddBook addBook = new AddBookUseCase(bookRepository, metadataLookup);
        LookupBookByIsbn lookupBookByIsbn = new LookupBookByIsbnUseCase(bookRepository, metadataLookup);
        DeleteBook deleteBook = new DeleteBookUseCase(bookRepository);

        app.get("/", ctx -> ctx.render("index.jte", Map.of("books", listBooks.execute())));
        app.get("/ping", ctx -> ctx.html("<p>Pong! " + Instant.now() + "</p>"));
        new BookController(getBookById, listBooks, addBook, lookupBookByIsbn, deleteBook).register(app);

        app.start(8080);
    }

    private static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env("BIBLION_DB_URL", "jdbc:postgresql://localhost:5432/biblion"));
        config.setUsername(env("BIBLION_DB_USER", "biblion"));
        config.setPassword(env("BIBLION_DB_PASSWORD", "biblion"));
        return new HikariDataSource(config);
    }

    private static void runMigrations(DataSource dataSource) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
