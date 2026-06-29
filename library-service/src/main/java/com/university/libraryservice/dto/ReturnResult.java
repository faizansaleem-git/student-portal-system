package com.university.libraryservice.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * ReturnResult — carries the outcome of a book return operation.
 * If overdue, fineAmount and invoiceReference are populated so the
 * controller can display the fine details to the student.
 */
@Data @Builder
public class ReturnResult {
    private boolean overdue;
    private BigDecimal fineAmount;
    private String invoiceReference;
    private String bookTitle;
}
