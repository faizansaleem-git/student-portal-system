package com.university.libraryservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Loan — JPA entity representing a book loan.
 *
 * When a book is returned after 14 days, a fine is calculated at £1/day overdue.
 * The fine is posted to the Finance service and the invoiceReference stored here
 * so the student can pay at the Finance portal.
 */
@Entity
@Table(name = "loans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Library account that borrowed this book. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private LibraryAccount account;

    /** The borrowed book. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt;

    /** Null until the book is returned. */
    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    /** Fine in GBP, null if returned on time or not yet returned. */
    @Column(name = "fine_amount", precision = 8, scale = 2)
    private BigDecimal fineAmount;

    /** Reference from Finance service, null if no fine was generated. */
    @Column(name = "invoice_reference", length = 64)
    private String invoiceReference;

    /** True if the loan is/was overdue at the time of return. */
    @Column(name = "is_overdue", nullable = false)
    private boolean overdue = false;
}
