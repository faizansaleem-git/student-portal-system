package com.university.libraryservice.repository;

import com.university.libraryservice.model.LibraryAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * LibraryAccountRepository — Spring Data JPA repository for LibraryAccount.
 * Interface only; Spring generates the implementation at runtime.
 */
public interface LibraryAccountRepository extends JpaRepository<LibraryAccount, Long> {
    Optional<LibraryAccount> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
}
