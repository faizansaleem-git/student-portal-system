package com.university.libraryservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * CreateAccountRequest — DTO for POST /api/accounts from the Student service.
 * Carries the studentId and the initial PIN (always "000000" on first enrolment).
 */
@Data
public class CreateAccountRequest {
    @NotBlank(message = "studentId is required")
    private String studentId;

    @NotBlank(message = "pin is required")
    private String pin;
}
