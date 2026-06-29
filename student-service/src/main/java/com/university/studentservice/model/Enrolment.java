package com.university.studentservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrolments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Enrolment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolment_date", nullable = false, updatable = false)
    private LocalDateTime enrolmentDate;

    @Column(name = "invoice_reference", length = 100)
    private String invoiceReference;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @PrePersist
    void prePersist() {
        this.enrolmentDate = LocalDateTime.now();
    }
}
