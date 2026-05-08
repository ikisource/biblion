CREATE TABLE book (
    id          BIGSERIAL PRIMARY KEY,
    title       TEXT NOT NULL,
    author      TEXT,
    year        INTEGER,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO book (title, author, year) VALUES
    ('Le Petit Prince', 'Antoine de Saint-Exupéry', 1943),
    ('L''Étranger', 'Albert Camus', 1942),
    ('Notre-Dame de Paris', 'Victor Hugo', 1831);
