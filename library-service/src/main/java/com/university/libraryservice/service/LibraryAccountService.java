package com.university.libraryservice.service;

import com.university.libraryservice.exception.LibraryException;
import com.university.libraryservice.model.LibraryAccount;
import com.university.libraryservice.repository.LibraryAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * LibraryAccountService — business logic for library accounts.
 *
 * Also implements UserDetailsService so Spring Security can load accounts
 * by studentId for form-login authentication.
 *
 * Constructor injection used throughout for explicit dependencies and testability.
 */
@Service
@Slf4j
public class LibraryAccountService implements UserDetailsService {

    private final LibraryAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor injection — makes dependencies explicit and allows
     * test doubles to be supplied without a full Spring context.
     */
    public LibraryAccountService(LibraryAccountRepository accountRepository,
                                 PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new library account for a student.
     * Idempotent: returns the existing account if studentId already registered.
     * Called by the Student service REST API on first enrolment.
     *
     * @param studentId the student's ID, e.g. "STU-00001"
     * @param rawPin    plain-text PIN — BCrypt-hashed before storage
     */
    @Transactional
    public LibraryAccount createAccount(String studentId, String rawPin) {
        return accountRepository.findByStudentId(studentId)
            .orElseGet(() -> {
                LibraryAccount account = LibraryAccount.builder()
                    .studentId(studentId)
                    .hashedPin(passwordEncoder.encode(rawPin))
                    .firstLogin(true)
                    .build();
                log.info("[LibraryAccount] Created account for {}", studentId);
                return accountRepository.save(account);
            });
    }

    /**
     * Validate a PIN against the stored BCrypt hash.
     *
     * @throws LibraryException if the account does not exist or PIN is wrong
     */
    public void validatePin(String studentId, String rawPin) {
        LibraryAccount account = accountRepository.findByStudentId(studentId)
            .orElseThrow(() -> new LibraryException("Account not found: " + studentId));
        if (!passwordEncoder.matches(rawPin, account.getHashedPin())) {
            throw new LibraryException("Invalid PIN");
        }
    }

    /**
     * Change a student's PIN after verifying the old one.
     * Marks isFirstLogin=false so the PIN-change prompt does not recur.
     */
    @Transactional
    public void changePin(String studentId, String oldPin, String newPin) {
        LibraryAccount account = accountRepository.findByStudentId(studentId)
            .orElseThrow(() -> new LibraryException("Account not found: " + studentId));
        if (!passwordEncoder.matches(oldPin, account.getHashedPin())) {
            throw new LibraryException("Current PIN is incorrect");
        }
        account.setHashedPin(passwordEncoder.encode(newPin));
        account.setFirstLogin(false);
        accountRepository.save(account);
        log.info("[LibraryAccount] PIN changed for {}", studentId);
    }

    public LibraryAccount findByStudentId(String studentId) {
        return accountRepository.findByStudentId(studentId)
            .orElseThrow(() -> new LibraryException("Account not found: " + studentId));
    }

    public List<LibraryAccount> findAll() {
        return accountRepository.findAll();
    }

    /**
     * UserDetailsService implementation — called by Spring Security during login.
     * studentId is the username; hashedPin is the password.
     * ROLE_ADMIN is assigned to the special "admin" account; all others get ROLE_STUDENT.
     */
    @Override
    public UserDetails loadUserByUsername(String studentId) throws UsernameNotFoundException {
        LibraryAccount account = accountRepository.findByStudentId(studentId)
            .orElseThrow(() -> new UsernameNotFoundException("No library account for: " + studentId));

        String role = "admin".equalsIgnoreCase(studentId) ? "ROLE_ADMIN" : "ROLE_STUDENT";

        return User.builder()
            .username(account.getStudentId())
            .password(account.getHashedPin())
            .authorities(new SimpleGrantedAuthority(role))
            .build();
    }
}
