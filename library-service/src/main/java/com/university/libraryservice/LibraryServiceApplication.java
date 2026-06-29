package com.university.libraryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LibraryServiceApplication — Spring Boot entry point for the Library microservice.
 *
 * Technology choices:
 *   - Spring Boot 3.5 / Java 21 (same stack as Student service for consistency)
 *   - MySQL 8 (relational schema suits books, loans, accounts with FK relationships)
 *   - Spring Security (session-based login with studentId + PIN)
 *   - Thymeleaf (server-side rendering matches the Student service UI approach)
 *   - Flyway (versioned DB migrations — schema reproducible from scratch)
 *   - RestTemplate (synchronous HTTP to Finance service for fine posting)
 */
@SpringBootApplication
public class LibraryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryServiceApplication.class, args);
    }
}
