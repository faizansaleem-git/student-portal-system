package com.university.libraryservice.controller;

import com.university.libraryservice.exception.LibraryException;
import com.university.libraryservice.service.BookService;
import com.university.libraryservice.service.LibraryAccountService;
import com.university.libraryservice.service.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AdminController — web UI controller for admin-only features.
 * Protected by ROLE_ADMIN in SecurityConfig.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final LibraryAccountService accountService;
    private final LoanService loanService;

    public AdminController(BookService bookService,
                           LibraryAccountService accountService,
                           LoanService loanService) {
        this.bookService = bookService;
        this.accountService = accountService;
        this.loanService = loanService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("accounts", accountService.findAll());
        model.addAttribute("activeLoans", loanService.findAllActiveLoans());
        model.addAttribute("overdueLoans", loanService.findOverdueLoans());
        return "admin/dashboard";
    }

    @GetMapping("/books/add")
    public String addBookPage() {
        return "admin/add-book";
    }

    @PostMapping("/books/add")
    public String addBook(@RequestParam String isbn,
                          @RequestParam int copies,
                          RedirectAttributes redirectAttrs) {
        try {
            var book = bookService.addByIsbn(isbn.trim(), copies);
            redirectAttrs.addFlashAttribute("success",
                "Added '" + book.getTitle() + "' by " + book.getAuthor() + " (" + copies + " copies).");
        } catch (LibraryException ex) {
            redirectAttrs.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/books/add";
    }
}
