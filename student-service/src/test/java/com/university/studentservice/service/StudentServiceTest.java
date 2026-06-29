package com.university.studentservice.service;

import com.university.studentservice.dto.ProfileUpdateRequest;
import com.university.studentservice.exception.StudentException;
import com.university.studentservice.model.Student;
import com.university.studentservice.model.User;
import com.university.studentservice.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository);
    }

    private Student makeStudent(Long userId) {
        User user = new User();
        try {
            var f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(user, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Student s = new Student();
        s.setUser(user);
        s.setFirstName("Jane");
        s.setLastName("Smith");
        s.setEmail("jane@test.com");
        return s;
    }

    @Test
    void findByUserId_whenExists_returnsStudent() {
        Student student = makeStudent(5L);
        when(studentRepository.findByUserId(5L)).thenReturn(Optional.of(student));

        Student result = studentService.findByUserId(5L);

        assertThat(result.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void findByUserId_whenNotFound_throwsException() {
        when(studentRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findByUserId(99L))
            .isInstanceOf(StudentException.class);
    }

    @Test
    void updateProfile_updatesAllFields() {
        Student student = makeStudent(3L);
        when(studentRepository.findByUserId(3L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setFirstName("Janet");
        req.setLastName("Jones");
        req.setEmail("janet@test.com");
        req.setDateOfBirth(LocalDate.of(1995, 6, 15));
        req.setProgramme("MSc Computer Science");

        Student result = studentService.updateProfile(3L, req);

        assertThat(result.getFirstName()).isEqualTo("Janet");
        assertThat(result.getLastName()).isEqualTo("Jones");
        assertThat(result.getEmail()).isEqualTo("janet@test.com");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 6, 15));
        assertThat(result.getProgramme()).isEqualTo("MSc Computer Science");
        verify(studentRepository).save(student);
    }
}
