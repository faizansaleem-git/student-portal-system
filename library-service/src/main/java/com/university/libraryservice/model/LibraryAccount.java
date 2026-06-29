package com.university.libraryservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * LibraryAccount — JPA entity representing a student's library account.
 *
 * Created by the Student service on first enrolment via POST /api/accounts.
 * PIN is BCrypt-hashed before storage (never stored in plain text).
 * isFirstLogin drives the mandatory PIN-change flow on first portal login.
 */
@Entity
@Table(name = "accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LibraryAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique student identifier, e.g. STU-00001. Supplied by Student service. */
    @Column(name = "student_id", unique = true, nullable = false, length = 20)
    private String studentId;

    /** BCrypt-hashed PIN — never exposed outside the service. */
    @Column(name = "hashed_pin", nullable = false)
    private String hashedPin;

    /**
     * True until the student logs in and changes from the default PIN.
     * Forces the PIN-change screen before the student can use the library portal.
     */
    @Column(name = "is_first_login", nullable = false)
    private boolean firstLogin = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }
}
