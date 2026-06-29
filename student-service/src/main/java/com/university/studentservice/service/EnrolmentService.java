package com.university.studentservice.service;

import com.university.studentservice.client.FinanceClient;
import com.university.studentservice.client.LibraryClient;
import com.university.studentservice.dto.GraduationStatus;
import com.university.studentservice.exception.StudentException;
import com.university.studentservice.model.Course;
import com.university.studentservice.model.Enrolment;
import com.university.studentservice.model.Student;
import com.university.studentservice.repository.EnrolmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EnrolmentService {

    private static final BigDecimal COURSE_FEE = new BigDecimal("1500.00");
    private static final int REQUIRED_CREDITS = 120;

    private final EnrolmentRepository enrolmentRepository;
    private final CourseService courseService;
    private final FinanceClient financeClient;
    private final LibraryClient libraryClient;

    public EnrolmentService(EnrolmentRepository enrolmentRepository,
                            CourseService courseService,
                            FinanceClient financeClient,
                            LibraryClient libraryClient) {
        this.enrolmentRepository = enrolmentRepository;
        this.courseService = courseService;
        this.financeClient = financeClient;
        this.libraryClient = libraryClient;
    }

    @Transactional
    public Enrolment enrol(Student student, Long courseId) {
        if (enrolmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new StudentException("Already enrolled in this course.");
        }

        Course course = courseService.findById(courseId);

        String invoiceRef = financeClient.createInvoice(
            student.getStudentId(),
            COURSE_FEE,
            "Enrolment fee: " + course.getTitle()
        );

        Enrolment enrolment = new Enrolment();
        enrolment.setStudent(student);
        enrolment.setCourse(course);
        enrolment.setInvoiceReference(invoiceRef);
        enrolment.setStatus("ACTIVE");
        return enrolmentRepository.save(enrolment);
    }

    public List<Enrolment> findByStudent(Student student) {
        return enrolmentRepository.findByStudentId(student.getId());
    }

    public GraduationStatus getGraduationStatus(Student student) {
        String studentId = student.getStudentId();

        int earnedCredits = enrolmentRepository.findByStudentId(student.getId())
            .stream()
            .mapToInt(e -> e.getCourse().getCredits())
            .sum();

        boolean hasUnpaidInvoices = financeClient.hasOutstandingBalance(studentId);
        boolean hasBorrowedBooks = libraryClient.hasActiveLoans(studentId);
        boolean creditsComplete = earnedCredits >= REQUIRED_CREDITS;
        boolean eligible = creditsComplete && !hasUnpaidInvoices && !hasBorrowedBooks;

        return GraduationStatus.builder()
            .eligible(eligible)
            .earnedCredits(earnedCredits)
            .requiredCredits(REQUIRED_CREDITS)
            .hasUnpaidInvoices(hasUnpaidInvoices)
            .hasBorrowedBooks(hasBorrowedBooks)
            .creditsComplete(creditsComplete)
            .build();
    }

    public boolean isEligibleForGraduation(Student student) {
        return getGraduationStatus(student).isEligible();
    }

    public long countActiveEnrolments(Student student) {
        return enrolmentRepository.countByStudentIdAndStatus(student.getId(), "ACTIVE");
    }
}
