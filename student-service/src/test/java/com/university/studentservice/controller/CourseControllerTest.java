package com.university.studentservice.controller;

import com.university.studentservice.client.FinanceClient;
import com.university.studentservice.client.LibraryClient;
import com.university.studentservice.model.Course;
import com.university.studentservice.model.Student;
import com.university.studentservice.model.User;
import com.university.studentservice.repository.EnrolmentRepository;
import com.university.studentservice.service.CourseService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private UserService userService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private EnrolmentRepository enrolmentRepository;

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
    void listCourses_returns200WithCourses() throws Exception {
        Student student = makeStudent();
        User user = new User();
        user.setUsername("testuser");

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(studentService.findByUserId(any())).thenReturn(student);
        when(enrolmentRepository.findByStudentId(any())).thenReturn(List.of());

        Course c = new Course();
        c.setTitle("Software Engineering");
        c.setDepartment("Computer Science");
        c.setCode("CS301");
        c.setCredits(20);
        when(courseService.search(null)).thenReturn(List.of(c));

        mockMvc.perform(get("/courses"))
            .andExpect(status().isOk())
            .andExpect(view().name("courses"))
            .andExpect(model().attributeExists("courses"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "STUDENT")
    void listCourses_withSearch_filtersResults() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        Student student = makeStudent();

        when(userService.findByUsername("testuser")).thenReturn(user);
        when(studentService.findByUserId(any())).thenReturn(student);
        when(enrolmentRepository.findByStudentId(any())).thenReturn(List.of());
        when(courseService.search("engineering")).thenReturn(List.of());

        mockMvc.perform(get("/courses").param("search", "engineering"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("search", "engineering"));
    }
}
