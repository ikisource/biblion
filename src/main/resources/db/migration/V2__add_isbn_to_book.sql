ALTER TABLE book ADD COLUMN isbn TEXT;

UPDATE book SET isbn = '9782070408504' WHERE title = 'Le Petit Prince';
UPDATE book SET isbn = '9782070360024' WHERE title = 'L''Étranger';
UPDATE book SET isbn = '9782253004226' WHERE title = 'Notre-Dame de Paris';

ALTER TABLE book ALTER COLUMN isbn SET NOT NULL;
ALTER TABLE book ADD CONSTRAINT book_isbn_unique UNIQUE (isbn);
