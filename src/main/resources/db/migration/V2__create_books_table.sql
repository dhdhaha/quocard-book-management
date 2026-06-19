CREATE TABLE books (
    id                  BIGSERIAL    PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    price               INTEGER      NOT NULL CHECK (price >= 0),
    publication_status  VARCHAR(20)  NOT NULL
);

CREATE INDEX idx_books_publication_status ON books (publication_status);
