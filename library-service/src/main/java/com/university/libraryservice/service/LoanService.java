package com.university.libraryservice.service;

import com.university.libraryservice.client.FinanceClient;
import com.university.libraryservice.dto.ReturnResult;
import com.university.libraryservice.exception.LibraryException;
import com.university.libraryservice.model.Book;
import com.university.libraryservice.model.LibraryAccount;
import com.university.libraryservice.model.Loan;
import com.university.libraryservice.repository.BookRepository;
import com.university.libraryservice.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * LoanService — business logic for borrowing and returning books.
 *
 * Key design decisions:
 *   - Loan period is 14 days; fine is £1.00 per day overdue.
 *   - On overdue return, a fine invoice is posted to Finance service via FinanceClient.
 *   - FinanceClient failure is handled gracefully: return still proceeds,
 *     and the student is told to contact the library about the fine.
 *
 * Constructor injection: all three dependencies are declared explicitly
 * so unit tests can supply mocks without Spring context.
 */
@Service
@Slf4j
public class LoanService {

    static final int LOAN_PERIOD_DAYS = 14;
    static final BigDecimal FINE_PER_DAY = new BigDecimal("1.00");

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final FinanceClient financeClient;

    /**
     * Constructor injection — promotes loose coupling and testability.
     * Dependencies injected by Spring; mocks injected in unit tests.
     */
    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       FinanceClient financeClient) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.financeClient = financeClient;
    }

    /**
     * Borrow a book by ISBN for a given account.
     * Decrements availableCopies; throws if no copies remain.
     */
    @Transactional
    public Loan borrowBook(LibraryAccount account, String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new LibraryException("Book not found: " + isbn));

        if (book.getAvailableCopies() <= 0) {
            throw new LibraryException("No copies available for: " + book.getTitle());
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan loan = Loan.builder()
            .account(account)
            .book(book)
            .borrowedAt(LocalDateTime.now())
            .build();

        log.info("[LoanService] {} borrowed '{}' (ISBN: {})", account.getStudentId(), book.getTitle(), isbn);
        return loanRepository.save(loan);
    }

    /**
     * Return a book by loanId.
     * Calculates overdue days; if overdue, posts fine to Finance service.
     * Returns a ReturnResult describing the outcome for the controller to display.
     */
    @Transactional
    public ReturnResult returnBook(Long loanId, LibraryAccount account) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new LibraryException("Loan not found: " + loanId));

        if (!loan.getAccount().getId().equals(account.getId())) {
            throw new LibraryException("This loan does not belong to your account");
        }
        if (loan.getReturnedAt() != null) {
            throw new LibraryException("This book has already been returned");
        }

        LocalDateTime now = LocalDateTime.now();
        long daysOut = ChronoUnit.DAYS.between(loan.getBorrowedAt(), now);
        boolean overdue = daysOut > LOAN_PERIOD_DAYS;

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        loan.setReturnedAt(now);

        if (overdue) {
            long daysOverdue = daysOut - LOAN_PERIOD_DAYS;
            BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(daysOverdue));
            String description = "Library fine - " + book.getTitle();
            String invoiceRef = financeClient.postFineInvoice(account.getStudentId(), fine, description);

            loan.setOverdue(true);
            loan.setFineAmount(fine);
            loan.setInvoiceReference(invoiceRef);

            log.info("[LoanService] Overdue return by {} — {} days, fine £{}, ref={}",
                account.getStudentId(), daysOverdue, fine, invoiceRef);

            loanRepository.save(loan);
            return ReturnResult.builder()
                .overdue(true)
                .fineAmount(fine)
                .invoiceReference(invoiceRef)
                .bookTitle(book.getTitle())
                .build();
        }

        loanRepository.save(loan);
        log.info("[LoanService] On-time return by {} of '{}'", account.getStudentId(), book.getTitle());
        return ReturnResult.builder().overdue(false).bookTitle(book.getTitle()).build();
    }

    public List<Loan> findActiveLoansForAccount(LibraryAccount account) {
        return loanRepository.findByAccountAndReturnedAtIsNull(account);
    }

    public List<Loan> findAllLoansForAccount(LibraryAccount account) {
        return loanRepository.findByAccount(account);
    }

    public List<Loan> findAllActiveLoans() {
        return loanRepository.findByReturnedAtIsNull();
    }

    public List<Loan> findOverdueLoans() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(LOAN_PERIOD_DAYS);
        return loanRepository.findOverdueLoans(cutoff);
    }
}
