package com.university.studentservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GraduationStatus {

    private static final int REQUIRED_CREDITS = 120;

    private boolean eligible;
    private int earnedCredits;
    private int requiredCredits;
    private boolean hasUnpaidInvoices;
    private boolean hasBorrowedBooks;
    private boolean creditsComplete;

    public static int getRequiredCredits() {
        return REQUIRED_CREDITS;
    }

    public int getCreditPercent() {
        if (requiredCredits == 0) return 0;
        return Math.min(100, earnedCredits * 100 / requiredCredits);
    }

    public int getRemainingCredits() {
        return Math.max(0, requiredCredits - earnedCredits);
    }
}
