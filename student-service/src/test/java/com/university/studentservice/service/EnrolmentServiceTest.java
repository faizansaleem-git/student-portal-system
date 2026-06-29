package com.university.studentservice.service;

import com.university.studentservice.client.FinanceClient;
import com.university.studentservice.client.LibraryClient;
import com.university.studentservice.exception.StudentException;
import com.university.studentservice.model.Course;
import com.university.studentservice.model.Enrolment;
import com.university.studentservice.model.Student;
import com.university.studentservice.model.User;
import com.university.studentservice.repository.EnrolmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrolmentServiceTest {

    @Mock
    private EnrolmentRepository enrolmentRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private FinanceClient financeClient;
    @Mock
    private LibraryClient libraryClient;

    private EnrolmentService enrolmentService;
    private Student student;

    @BeforeEach
    void setUp() {
        enrolmentService = new EnrolmentService(enrolmentRepository, courseService, financeClient, libraryClient);

        User user = new User();
        user.setUsername("john");
        student = new Student();
        // Set id via reflection so getStudentId() works
        try {
            var field = Student.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(student, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        student.setUser(user);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john@test.com");
    }

    @Test
    void enrol_happyPath_createsEnrolmentWithInvoiceRef() {
        Course course = new Course();
        course.setTitle("Software Engineering");

        when(enrolmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);
        when(courseService.findById(10L)).thenReturn(course);
        when(financeClient.createInvoice(eq("STU-00001"), any(BigDecimal.class), anyString()))
            .thenReturn("INV-ABC123");

        Enrolment saved = new Enrolment();
        saved.setInvoiceReference("INV-ABC123");
        saved.setCourse(course);
        when(enrolmentRepository.save(any())).thenReturn(saved);

        Enrolment result = enrolmentService.enrol(student, 10L);

        assertThat(result.getInvoiceReference()).isEqualTo("INV-ABC123");
        verify(financeClient).createInvoice(eq("STU-00001"), eq(new BigDecimal("1500.00")),
                contains("Software Engineering"));
    }

    @Test
    void enrol_alreadyEnrolled_throwsException() {
        when(enrolmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> enrolmentService.enrol(student, 10L))
            .isInstanceOf(StudentException.class)
            .hasMessageContaining("Already enrolled");
    }

    @Test
    void enrol_financeDown_savesWithNullRef() {
        Course course = new Course();
        course.setTitle("Algorithms");

        when(enrolmentRepository.existsByStudentIdAndCourseId(1L, 20L)).thenReturn(false);
        when(courseService.findById(20L)).thenReturn(course);
        when(financeClient.createInvoice(any(), any(), any())).thenReturn(null);

        Enrolment saved = new Enrolment();
        saved.setInvoiceReference(null);
        saved.setCourse(course);
        when(enrolmentRepository.save(any())).thenReturn(saved);

        Enrolment result = enrolmentService.enrol(student, 20L);

        assertThat(result.getInvoiceReference()).isNull();
    }

    @Test
    void isEligibleForGraduation_noOutstandingNoLoans_returnsTrue() {
        when(enrolmentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(financeClient.hasOutstandingBalance("STU-00001")).thenReturn(false);
        when(libraryClient.hasActiveLoans("STU-00001")).thenReturn(false);

        assertThat(enrolmentService.isEligibleForGraduation(student)).isFalse(); // 0 credits < 120 required
    }

    @Test
    void isEligibleForGraduation_outstandingInvoice_returnsFalse() {
        when(enrolmentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(financeClient.hasOutstandingBalance("STU-00001")).thenReturn(true);
        when(libraryClient.hasActiveLoans("STU-00001")).thenReturn(false);

        assertThat(enrolmentService.isEligibleForGraduation(student)).isFalse();
    }

    @Test
    void isEligibleForGraduation_activeLoans_returnsFalse() {
        when(enrolmentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(financeClient.hasOutstandingBalance("STU-00001")).thenReturn(false);
        when(libraryClient.hasActiveLoans("STU-00001")).thenReturn(true);

        assertThat(enrolmentService.isEligibleForGraduation(student)).isFalse();
    }

    @Test
    void findByStudent_returnsEnrolmentList() {
        Enrolment e = new Enrolment();
        when(enrolmentRepository.findByStudentId(1L)).thenReturn(List.of(e));

        List<Enrolment> result = enrolmentService.findByStudent(student);

        assertThat(result).hasSize(1);
    }
}
