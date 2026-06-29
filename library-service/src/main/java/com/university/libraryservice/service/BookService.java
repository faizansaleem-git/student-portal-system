package com.university.libraryservice.service;

import com.university.libraryservice.dto.OpenLibraryResponse;
import com.university.libraryservice.exception.LibraryException;
import com.university.libraryservice.model.Book;
import com.university.libraryservice.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * BookService — business logic for the book catalogue.
 *
 * Admin "add by ISBN" fetches title/author from the Open Library API,
 * demonstrating real third-party service integration.
 *
 * Constructor injection used throughout for testability.
 */
@Service
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    public BookService(BookRepository bookRepository, RestTemplate restTemplate) {
        this.bookRepository = bookRepository;
        this.restTemplate = restTemplate;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public List<Book> search(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new LibraryException("Book not found: ISBN " + isbn));
    }

    /**
     * Add a book by ISBN.
     * Fetches title and author from Open Library API, then saves with the given number of copies.
     * Throws LibraryException if the ISBN is not found on Open Library or already exists.
     */
    @Transactional
    public Book addByIsbn(String isbn, int copies) {
        if (bookRepository.findByIsbn(isbn).isPresent()) {
            throw new LibraryException("Book already exists with ISBN: " + isbn);
        }

        // Open Library Books API — returns a map keyed by "ISBN:<isbn>"
        String url = "https://openlibrary.org/api/books?bibkeys=ISBN:" + isbn + "&format=json&jscmd=data";
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || response.isEmpty()) {
                throw new LibraryException("ISBN not found on Open Library: " + isbn);
            }
            // The key is "ISBN:<isbn>"
            @SuppressWarnings("unchecked")
            Map<String, Object> bookData = (Map<String, Object>) response.get("ISBN:" + isbn);
            if (bookData == null) {
                throw new LibraryException("ISBN not found on Open Library: " + isbn);
            }

            String title = (String) bookData.getOrDefault("title", "Unknown Title");

            // authors is a list of maps with "name" key
            String author = "Unknown Author";
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> authors = (List<Map<String, Object>>) bookData.get("authors");
            if (authors != null && !authors.isEmpty()) {
                author = (String) authors.get(0).getOrDefault("name", "Unknown Author");
            }

            Book book = Book.builder()
                .isbn(isbn)
                .title(title)
                .author(author)
                .totalCopies(copies)
                .availableCopies(copies)
                .build();

            log.info("[BookService] Added book: '{}' by {} (ISBN: {})", title, author, isbn);
            return bookRepository.save(book);

        } catch (RestClientException ex) {
            log.error("[BookService] Failed to fetch ISBN {} from Open Library: {}", isbn, ex.getMessage());
            throw new LibraryException("Could not reach Open Library API. Please try again.");
        }
    }
}
