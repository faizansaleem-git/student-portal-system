package com.university.libraryservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * AuthController — handles the login page (GET only; POST is handled by Spring Security).
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) model.addAttribute("error", "Invalid student ID or PIN. Please try again.");
        if (logout != null) model.addAttribute("message", "You have been logged out.");
        return "auth/login";
    }

    @GetMapping("/health")
    public org.springframework.http.ResponseEntity<java.util.Map<String,String>> health() {
        return org.springframework.http.ResponseEntity.ok(
            java.util.Map.of("status","UP","service","library-service"));
    }
}
