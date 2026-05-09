ALTER TABLE book ALTER COLUMN isbn DROP NOT NULL;

ALTER TABLE book DROP CONSTRAINT book_isbn_unique;

CREATE UNIQUE INDEX book_isbn_unique ON book (isbn) WHERE isbn IS NOT NULL;
