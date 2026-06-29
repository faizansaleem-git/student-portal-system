package com.university.libraryservice.repository;

import com.university.libraryservice.model.Loan;
import com.university.libraryservice.model.LibraryAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * LoanRepository — Spring Data JPA repository for Loan.
 */
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /** All loans (active and returned) for a given account. */
    List<Loan> findByAccount(LibraryAccount account);

    /** Active (not yet returned) loans for a given account. */
    List<Loan> findByAccountAndReturnedAtIsNull(LibraryAccount account);

    /** All currently active loans across all students — for admin view. */
    List<Loan> findByReturnedAtIsNull();

    /** Loans that are still active and were borrowed before a given cutoff — for overdue admin view. */
    @Query("SELECT l FROM Loan l WHERE l.returnedAt IS NULL AND l.borrowedAt < :cutoff")
    List<Loan> findOverdueLoans(@org.springframework.data.repository.query.Param("cutoff") java.time.LocalDateTime cutoff);
}
