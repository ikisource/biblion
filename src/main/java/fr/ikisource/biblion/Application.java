package fr.ikisource.biblion;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.ikisource.biblion.book.application.GetBookByIdUseCase;
import fr.ikisource.biblion.book.domain.api.GetBookById;
import fr.ikisource.biblion.book.domain.spi.BookRepository;
import fr.ikisource.biblion.book.infrastructure.BookController;
import fr.ikisource.biblion.book.infrastructure.BookJooqRepository;
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
import java.util.List;
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

        app.get("/", ctx -> {
            List<String> titles = db.select(DSL.field("title", String.class))
                    .from(DSL.table("book"))
                    .orderBy(DSL.field("created_at").desc())
                    .fetch(DSL.field("title", String.class));
            ctx.render("index.jte", Map.of("title", "Biblion", "books", titles));
        });

        app.get("/ping", ctx -> ctx.html("<p>Pong! " + Instant.now() + "</p>"));

        BookRepository bookRepository = new BookJooqRepository(db);
        GetBookById getBookById = new GetBookByIdUseCase(bookRepository);
        new BookController(getBookById).register(app);

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
