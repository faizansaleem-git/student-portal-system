package com.university.libraryservice.exception;

/**
 * LibraryException — domain exception for expected error conditions in the library.
 * Examples: book not available, loan not found, invalid PIN.
 */
public class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }
}
