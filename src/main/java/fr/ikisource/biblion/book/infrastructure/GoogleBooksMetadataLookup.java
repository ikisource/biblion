package fr.ikisource.biblion.book.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ikisource.biblion.book.domain.BookMetadata;
import fr.ikisource.biblion.book.domain.Isbn;
import fr.ikisource.biblion.book.domain.spi.BookMetadataLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GoogleBooksMetadataLookup implements BookMetadataLookup {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksMetadataLookup.class);
    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient http;
    private final ObjectMapper json;

    public GoogleBooksMetadataLookup() {
        this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), new ObjectMapper());
    }

    GoogleBooksMetadataLookup(HttpClient http, ObjectMapper json) {
        this.http = http;
        this.json = json;
    }

    @Override
    public Optional<BookMetadata> findByIsbn(Isbn isbn) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + isbn.value()))
                .timeout(TIMEOUT)
                .GET()
                .build();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Google Books returned {} for ISBN {}", response.statusCode(), isbn.value());
                return Optional.empty();
            }
            return parse(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lookup interrupted for ISBN {}", isbn.value());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Lookup failed for ISBN {}: {}", isbn.value(), e.toString());
            return Optional.empty();
        }
    }

    private Optional<BookMetadata> parse(String body) throws Exception {
        JsonNode root = json.readTree(body);
        JsonNode items = root.path("items");
        if (!items.isArray() || items.isEmpty()) {
            return Optional.empty();
        }
        JsonNode info = items.get(0).path("volumeInfo");
        String title = info.path("title").asText(null);
        if (title == null) {
            return Optional.empty();
        }
        String author = null;
        JsonNode authors = info.path("authors");
        if (authors.isArray() && !authors.isEmpty()) {
            author = StreamSupport.stream(authors.spliterator(), false)
                    .map(JsonNode::asText)
                    .collect(Collectors.joining(", "));
        }
        return Optional.of(new BookMetadata(title, author));
    }
}
