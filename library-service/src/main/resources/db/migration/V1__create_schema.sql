-- V1__create_schema.sql
-- Library Service database schema
-- Flyway runs this on first startup to create all tables.

CREATE TABLE IF NOT EXISTS accounts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id    VARCHAR(20)  NOT NULL UNIQUE,
    hashed_pin    VARCHAR(100) NOT NULL,
    is_first_login TINYINT(1)  NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS books (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn             VARCHAR(20)  NOT NULL UNIQUE,
    title            VARCHAR(255) NOT NULL,
    author           VARCHAR(255) NOT NULL,
    total_copies     INT          NOT NULL DEFAULT 1,
    available_copies INT          NOT NULL DEFAULT 1,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS loans (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id        BIGINT          NOT NULL,
    book_id           BIGINT          NOT NULL,
    borrowed_at       DATETIME        NOT NULL,
    returned_at       DATETIME        NULL,
    fine_amount       DECIMAL(8,2)    NULL,
    invoice_reference VARCHAR(64)     NULL,
    is_overdue        TINYINT(1)      NOT NULL DEFAULT 0,
    CONSTRAINT fk_loan_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_loan_book    FOREIGN KEY (book_id)    REFERENCES books(id)
);
