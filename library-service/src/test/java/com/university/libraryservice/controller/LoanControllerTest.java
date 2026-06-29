package com.university.libraryservice.controller;

import com.university.libraryservice.client.FinanceClient;
import com.university.libraryservice.model.Book;
import com.university.libraryservice.model.LibraryAccount;
import com.university.libraryservice.model.Loan;
import com.university.libraryservice.repository.BookRepository;
import com.university.libraryservice.repository.LibraryAccountRepository;
import com.university.libraryservice.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LoanControllerTest — integration test for the book return flow.
 *
 * Uses @SpringBootTest with H2 in-memory database (see test application.properties).
 * FinanceClient is @MockBean so no real HTTP call is made to Finance service.
 * Verifies that returning an overdue book triggers the fine flow end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class LoanControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired LibraryAccountRepository accountRepository;
    @Autowired BookRepository bookRepository;
    @Autowired LoanRepository loanRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean FinanceClient financeClient;

    LibraryAccount testAccount;
    Book testBook;
    Loan overdueLoan;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        accountRepository.deleteAll();

        testAccount = accountRepository.save(LibraryAccount.builder()
            .studentId("STU-TEST")
            .hashedPin(passwordEncoder.encode("123456"))
            .firstLogin(false)
            .build());

        testBook = bookRepository.save(Book.builder()
            .isbn("9780000000001")
            .title("Test Book")
            .author("Test Author")
            .totalCopies(3)
            .availableCopies(2)
            .build());

        overdueLoan = loanRepository.save(Loan.builder()
            .account(testAccount)
            .book(testBook)
            .borrowedAt(LocalDateTime.now().minusDays(20))
            .build());
    }

    @Test
    @WithMockUser(username = "STU-TEST", roles = "STUDENT")
    void returnOverdueBook_triggersFineFineAndRedirects() throws Exception {
        when(financeClient.postFineInvoice(eq("STU-TEST"), any(BigDecimal.class), contains("Test Book")))
            .thenReturn("fine-ref-001");

        mockMvc.perform(post("/return/" + overdueLoan.getId()).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @WithMockUser(username = "STU-TEST", roles = "STUDENT")
    void returnOnTimeBook_noFineRedirects() throws Exception {
        Loan recentLoan = loanRepository.save(Loan.builder()
            .account(testAccount)
            .book(testBook)
            .borrowedAt(LocalDateTime.now().minusDays(3))
            .build());

        mockMvc.perform(post("/return/" + recentLoan.getId()).with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));
    }
}
