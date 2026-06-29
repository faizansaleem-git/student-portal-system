package com.university.libraryservice.controller;

import com.university.libraryservice.dto.CreateAccountRequest;
import com.university.libraryservice.model.LibraryAccount;
import com.university.libraryservice.service.LibraryAccountService;
import com.university.libraryservice.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ApiController — REST API endpoints consumed by the Student service.
 *
 * These are machine-to-machine endpoints (not browser-facing).
 * No Spring Security authentication required (permitted in SecurityConfig).
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final LibraryAccountService libraryAccountService;
    private final LoanService loanService;

    public ApiController(LibraryAccountService libraryAccountService, LoanService loanService) {
        this.libraryAccountService = libraryAccountService;
        this.loanService = loanService;
    }

    /**
     * POST /api/accounts
     * Called by Student service when a student first enrols in any course.
     * Creates a library account with the initial PIN (always "000000").
     */
    @PostMapping("/accounts")
    public ResponseEntity<Map<String, String>> createAccount(@Valid @RequestBody CreateAccountRequest req) {
        LibraryAccount account = libraryAccountService.createAccount(req.getStudentId(), req.getPin());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("studentId", account.getStudentId(), "message", "Library account created"));
    }

    /**
     * GET /api/accounts/{studentId}/loans/active-count
     * Called by Student service to check if student has unreturned books (blocks graduation).
     */
    @GetMapping("/accounts/{studentId}/loans/active-count")
    public ResponseEntity<Map<String, Object>> activeLoansCount(@PathVariable String studentId) {
        try {
            LibraryAccount account = libraryAccountService.findByStudentId(studentId);
            long count = loanService.findActiveLoansForAccount(account).size();
            return ResponseEntity.ok(Map.of("studentId", studentId, "activeLoans", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("studentId", studentId, "activeLoans", 0));
        }
    }

    /**
     * GET /health
     * Docker healthcheck endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "library-service"));
    }
}
