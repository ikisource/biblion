package fr.ikisource.biblion.book.domain.api;

import fr.ikisource.biblion.book.domain.BookMetadata;
import fr.ikisource.biblion.book.domain.Isbn;

public interface LookupBookByIsbn {

    Result execute(Isbn isbn);

    record Result(State state, BookMetadata metadata) {

        public enum State { ALREADY_IN_LIBRARY, FOUND, NOT_FOUND }

        public static Result alreadyInLibrary() {
            return new Result(State.ALREADY_IN_LIBRARY, null);
        }

        public static Result found(BookMetadata metadata) {
            return new Result(State.FOUND, metadata);
        }

        public static Result notFound() {
            return new Result(State.NOT_FOUND, null);
        }
    }
}
