package com.university.libraryservice.service;

import com.university.libraryservice.exception.LibraryException;
import com.university.libraryservice.model.LibraryAccount;
import com.university.libraryservice.repository.LibraryAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * LibraryAccountServiceTest — unit tests for LibraryAccountService.
 *
 * Uses a real BCryptPasswordEncoder (not mocked) so PIN hashing/verification
 * is actually exercised.
 */
@ExtendWith(MockitoExtension.class)
class LibraryAccountServiceTest {

    @Mock LibraryAccountRepository accountRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4); // low cost for tests
    LibraryAccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new LibraryAccountService(accountRepository, passwordEncoder);
    }

    @Test
    void createAccount_savesNewAccountWithHashedPin() {
        when(accountRepository.findByStudentId("STU-00001")).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LibraryAccount result = accountService.createAccount("STU-00001", "000000");

        assertThat(result.getStudentId()).isEqualTo("STU-00001");
        assertThat(result.isFirstLogin()).isTrue();
        // PIN must be stored as BCrypt hash, not plain text
        assertThat(result.getHashedPin()).startsWith("$2a$");
        assertThat(passwordEncoder.matches("000000", result.getHashedPin())).isTrue();
        verify(accountRepository).save(any());
    }

    @Test
    void createAccount_idempotent_returnsExistingIfAlreadyExists() {
        LibraryAccount existing = LibraryAccount.builder()
            .id(1L).studentId("STU-00001").hashedPin("existing-hash").build();
        when(accountRepository.findByStudentId("STU-00001")).thenReturn(Optional.of(existing));

        LibraryAccount result = accountService.createAccount("STU-00001", "000000");

        assertThat(result).isSameAs(existing);
        verify(accountRepository, never()).save(any()); // must NOT save again
    }

    @Test
    void validatePin_correctPin_doesNotThrow() {
        String hashed = passwordEncoder.encode("123456");
        LibraryAccount account = LibraryAccount.builder()
            .studentId("STU-00001").hashedPin(hashed).build();
        when(accountRepository.findByStudentId("STU-00001")).thenReturn(Optional.of(account));

        assertThatCode(() -> accountService.validatePin("STU-00001", "123456"))
            .doesNotThrowAnyException();
    }

    @Test
    void validatePin_wrongPin_throwsLibraryException() {
        String hashed = passwordEncoder.encode("123456");
        LibraryAccount account = LibraryAccount.builder()
            .studentId("STU-00001").hashedPin(hashed).build();
        when(accountRepository.findByStudentId("STU-00001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.validatePin("STU-00001", "999999"))
            .isInstanceOf(LibraryException.class)
            .hasMessageContaining("Invalid PIN");
    }

    @Test
    void validatePin_unknownStudentId_throwsLibraryException() {
        when(accountRepository.findByStudentId("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.validatePin("UNKNOWN", "000000"))
            .isInstanceOf(LibraryException.class)
            .hasMessageContaining("Account not found");
    }
}
