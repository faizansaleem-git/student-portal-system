package com.university.studentservice.controller;

import com.university.studentservice.client.FinanceClient;
import com.university.studentservice.client.LibraryClient;
import com.university.studentservice.dto.GraduationStatus;
import com.university.studentservice.model.Enrolment;
import com.university.studentservice.model.Course;
import com.university.studentservice.model.Student;
import com.university.studentservice.model.User;
import com.university.studentservice.service.EnrolmentService;
import com.university.studentservice.service.StudentService;
import com.university.studentservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class EnrolmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrolmentService enrolmentService;

    @MockBean
    private UserService userService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private FinanceClient financeClient;

    @MockBean
    private LibraryClient libraryClient;

    private Student makeStudent() {
        User user = new User();
        user.setUsername("testuser");
        Student s = new Student();
        s.setUser(user);
        s.setFirstName("Test");
        s.setLastName("User");
        s.setEmail("test@test.com");
        return s;
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void myEnrolments_returns200WithList() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        Student student = makeStudent();

        Course course = new Course();
        course.setTitle("Algorithms");
        course.setCode("CS201");
        course.setDepartment("CS");
        course.setCredits(20);

        Enrolment e = new Enrolment();
        e.setStudent(student);
        e.setCourse(course);
        e.setInvoiceReference("INV-TEST01");
        e.setStatus("ACTIVE");

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(studentService.findByUserId(any())).thenReturn(student);
        when(enrolmentService.findByStudent(student)).thenReturn(List.of(e));

        mockMvc.perform(get("/enrolments"))
            .andExpect(status().isOk())
            .andExpect(view().name("enrolments"))
            .andExpect(model().attributeExists("enrolments"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void graduation_eligible_showsEligible() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        Student student = makeStudent();

        GraduationStatus status = GraduationStatus.builder()
            .eligible(true).earnedCredits(120).requiredCredits(120)
            .creditsComplete(true).hasUnpaidInvoices(false).hasBorrowedBooks(false)
            .build();

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(studentService.findByUserId(any())).thenReturn(student);
        when(enrolmentService.getGraduationStatus(student)).thenReturn(status);

        mockMvc.perform(get("/enrolments/graduation"))
            .andExpect(status().isOk())
            .andExpect(view().name("graduation"))
            .andExpect(model().attributeExists("status"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void graduation_notEligible_showsNotEligible() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        Student student = makeStudent();

        GraduationStatus status = GraduationStatus.builder()
            .eligible(false).earnedCredits(60).requiredCredits(120)
            .creditsComplete(false).hasUnpaidInvoices(true).hasBorrowedBooks(false)
            .build();

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(studentService.findByUserId(any())).thenReturn(student);
        when(enrolmentService.getGraduationStatus(student)).thenReturn(status);

        mockMvc.perform(get("/enrolments/graduation"))
            .andExpect(status().isOk())
            .andExpect(view().name("graduation"))
            .andExpect(model().attributeExists("status"));
    }
}
