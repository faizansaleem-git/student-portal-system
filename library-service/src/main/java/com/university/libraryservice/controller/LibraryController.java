package com.university.libraryservice.controller;

import com.university.libraryservice.dto.ReturnResult;
import com.university.libraryservice.exception.LibraryException;
import com.university.libraryservice.model.Book;
import com.university.libraryservice.model.LibraryAccount;
import com.university.libraryservice.model.Loan;
import com.university.libraryservice.service.BookService;
import com.university.libraryservice.service.LibraryAccountService;
import com.university.libraryservice.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * LibraryController — web UI controller for student-facing library features.
 *
 * No business logic here — delegates entirely to services.
 * @AuthenticationPrincipal injects the logged-in user's UserDetails.
 */
@Controller
public class LibraryController {

    private final LibraryAccountService accountService;
    private final BookService bookService;
    private final LoanService loanService;

    /**
     * Constructor injection — three explicit dependencies, all mockable in tests.
     */
    public LibraryController(LibraryAccountService accountService,
                             BookService bookService,
                             LoanService loanService) {
        this.accountService = accountService;
        this.bookService = bookService;
        this.loanService = loanService;
    }

    // ── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails user, Model model) {
        LibraryAccount account = accountService.findByStudentId(user.getUsername());
        if (account.isFirstLogin()) {
            return "redirect:/change-pin";
        }
        List<Loan> activeLoans = loanService.findActiveLoansForAccount(account);
        model.addAttribute("account", account);
        model.addAttribute("activeLoans", activeLoans);
        return "library/dashboard";
    }

    // ── PIN change ───────────────────────────────────────────────────────────

    @GetMapping("/change-pin")
    public String changePinPage() {
        return "auth/change-pin";
    }

    @PostMapping("/change-pin")
    public String changePin(@AuthenticationPrincipal UserDetails user,
                            @RequestParam String oldPin,
                            @RequestParam String newPin,
                            @RequestParam String confirmPin,
                            HttpSession session,
                            RedirectAttributes redirectAttrs) {
        if (!newPin.equals(confirmPin)) {
            redirectAttrs.addFlashAttribute("error", "New PIN and confirmation do not match.");
            return "redirect:/change-pin";
        }
        try {
            accountService.changePin(user.getUsername(), oldPin, newPin);
            SecurityContextHolder.clearContext();
            session.invalidate();
            return "redirect:/login?pinChanged=true";
        } catch (LibraryException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
            return "redirect:/change-pin";
        }
    }

    // ── Books ────────────────────────────────────────────────────────────────

    @GetMapping("/books")
    public String books(@RequestParam(required = false) String search, Model model) {
        List<Book> books = (search != null && !search.isBlank())
            ? bookService.search(search)
            : bookService.findAll();
        model.addAttribute("books", books);
        model.addAttribute("search", search);
        return "library/books";
    }

    // ── Borrow ───────────────────────────────────────────────────────────────

    @PostMapping("/borrow/{isbn}")
    public String borrow(@AuthenticationPrincipal UserDetails user,
                         @PathVariable String isbn,
                         RedirectAttributes redirectAttrs) {
        LibraryAccount account = accountService.findByStudentId(user.getUsername());
        try {
            loanService.borrowBook(account, isbn);
            redirectAttrs.addFlashAttribute("success", "Book borrowed successfully! Return within 14 days.");
        } catch (LibraryException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/books";
    }

    // ── Return ───────────────────────────────────────────────────────────────

    @PostMapping("/return/{loanId}")
    public String returnBook(@AuthenticationPrincipal UserDetails user,
                             @PathVariable Long loanId,
                             RedirectAttributes redirectAttrs) {
        LibraryAccount account = accountService.findByStudentId(user.getUsername());
        try {
            ReturnResult result = loanService.returnBook(loanId, account);
            if (result.isOverdue()) {
                String msg = String.format(
                    "Book returned late. Fine: £%.2f for '%s'. Invoice ref: %s. Pay at Finance Portal.",
                    result.getFineAmount(), result.getBookTitle(),
                    result.getInvoiceReference() != null ? result.getInvoiceReference() : "N/A (Finance unavailable)"
                );
                redirectAttrs.addFlashAttribute("warning", msg);
            } else {
                redirectAttrs.addFlashAttribute("success", "'" + result.getBookTitle() + "' returned on time. Thank you!");
            }
        } catch (LibraryException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ── Loan history ─────────────────────────────────────────────────────────

    @GetMapping("/my-loans")
    public String myLoans(@AuthenticationPrincipal UserDetails user, Model model) {
        LibraryAccount account = accountService.findByStudentId(user.getUsername());
        model.addAttribute("loans", loanService.findAllLoansForAccount(account));
        return "library/my-loans";
    }
}
