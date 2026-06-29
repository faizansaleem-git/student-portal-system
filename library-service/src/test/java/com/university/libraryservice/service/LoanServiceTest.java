package com.university.libraryservice.service;

import com.university.libraryservice.client.FinanceClient;
import com.university.libraryservice.dto.ReturnResult;
import com.university.libraryservice.exception.LibraryException;
import com.university.libraryservice.model.Book;
import com.university.libraryservice.model.LibraryAccount;
import com.university.libraryservice.model.Loan;
import com.university.libraryservice.repository.BookRepository;
import com.university.libraryservice.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoanServiceTest — unit tests for LoanService.
 *
 * All dependencies are mocked with Mockito (no Spring context, no database).
 * Tests verify the two core business scenarios:
 *   - On-time return: no fine, no Finance call
 *   - Late return: fine calculated, Finance client called
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock LoanRepository loanRepository;
    @Mock BookRepository bookRepository;
    @Mock FinanceClient financeClient;

    LoanService loanService;

    LibraryAccount account;
    Book book;

    @BeforeEach
    void setUp() {
        // Constructor injection — mirrors production wiring, no Spring context needed
        loanService = new LoanService(loanRepository, bookRepository, financeClient);

        account = LibraryAccount.builder()
            .id(1L).studentId("STU-00001").hashedPin("hash").firstLogin(false).build();

        book = Book.builder()
            .id(1L).isbn("9780132350884").title("Clean Code")
            .author("Robert C. Martin").totalCopies(4).availableCopies(3).build();
    }

    // ── returnBook: on time ───────────────────────────────────────────────────

    @Test
    void returnOnTime_noFineAndFinanceNotCalled() {
        Loan loan = Loan.builder()
            .id(10L)
            .account(account)
            .book(book)
            .borrowedAt(LocalDateTime.now().minusDays(7)) // 7 days — within 14-day limit
            .build();

        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(bookRepository.save(any())).thenReturn(book);

        ReturnResult result = loanService.returnBook(10L, account);

        assertThat(result.isOverdue()).isFalse();
        assertThat(result.getFineAmount()).isNull();
        assertThat(result.getBookTitle()).isEqualTo("Clean Code");

        // Finance client must NOT be called for on-time returns
        verifyNoInteractions(financeClient);
        verify(loanRepository).save(loan);
    }

    // ── returnBook: overdue ───────────────────────────────────────────────────

    @Test
    void returnLate_fineCalculatedAndFinanceClientCalled() {
        Loan loan = Loan.builder()
            .id(20L)
            .account(account)
            .book(book)
            .borrowedAt(LocalDateTime.now().minusDays(20)) // 20 days — 6 days overdue
            .build();

        when(loanRepository.findById(20L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(bookRepository.save(any())).thenReturn(book);
        when(financeClient.postFineInvoice(anyString(), any(BigDecimal.class), anyString()))
            .thenReturn("ref-abc-123");

        ReturnResult result = loanService.returnBook(20L, account);

        assertThat(result.isOverdue()).isTrue();
        // 20 days borrowed − 14 day period = 6 days overdue × £1.00 = £6.00
        assertThat(result.getFineAmount()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(result.getInvoiceReference()).isEqualTo("ref-abc-123");

        // Finance client MUST be called with correct studentId and amount
        verify(financeClient).postFineInvoice(
            eq("STU-00001"),
            eq(new BigDecimal("6.00")),
            contains("Clean Code")
        );
    }

    @Test
    void returnLate_financeClientFailure_returnStillCompletes() {
        Loan loan = Loan.builder()
            .id(30L)
            .account(account)
            .book(book)
            .borrowedAt(LocalDateTime.now().minusDays(20))
            .build();

        when(loanRepository.findById(30L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenReturn(loan);
        when(bookRepository.save(any())).thenReturn(book);
        when(financeClient.postFineInvoice(anyString(), any(), anyString()))
            .thenReturn(null); // Finance service unavailable

        ReturnResult result = loanService.returnBook(30L, account);

        // Return still completes even if Finance is down
        assertThat(result.isOverdue()).isTrue();
        assertThat(result.getInvoiceReference()).isNull();
    }

    // ── borrowBook ────────────────────────────────────────────────────────────

    @Test
    void borrowBook_noAvailableCopies_throwsLibraryException() {
        book.setAvailableCopies(0);
        when(bookRepository.findByIsbn("9780132350884")).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> loanService.borrowBook(account, "9780132350884"))
            .isInstanceOf(LibraryException.class)
            .hasMessageContaining("No copies available");
    }

    @Test
    void returnBook_wrongAccount_throwsLibraryException() {
        LibraryAccount otherAccount = LibraryAccount.builder().id(99L).studentId("STU-99999").build();
        Loan loan = Loan.builder().id(40L).account(account).book(book)
            .borrowedAt(LocalDateTime.now().minusDays(1)).build();

        when(loanRepository.findById(40L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnBook(40L, otherAccount))
            .isInstanceOf(LibraryException.class)
            .hasMessageContaining("does not belong");
    }
}
