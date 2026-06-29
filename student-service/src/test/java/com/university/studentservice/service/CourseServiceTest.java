package com.university.studentservice.service;

import com.university.studentservice.exception.StudentException;
import com.university.studentservice.model.Course;
import com.university.studentservice.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseRepository);
    }

    @Test
    void findAll_returnsCourseList() {
        Course c = new Course();
        c.setTitle("Software Engineering");
        when(courseRepository.findAll()).thenReturn(List.of(c));

        List<Course> result = courseService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Software Engineering");
    }

    @Test
    void search_withBlankQuery_returnsAll() {
        when(courseRepository.findAll()).thenReturn(List.of(new Course()));

        List<Course> result = courseService.search("  ");

        verify(courseRepository).findAll();
        verifyNoMoreInteractions(courseRepository);
        assertThat(result).hasSize(1);
    }

    @Test
    void search_withQuery_delegatesToRepository() {
        Course c = new Course();
        c.setTitle("Computer Science");
        when(courseRepository.findByTitleContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
                "computer", "computer")).thenReturn(List.of(c));

        List<Course> result = courseService.search("computer");

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_whenExists_returnsCourse() {
        Course c = new Course();
        c.setTitle("Algorithms");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(c));

        Course result = courseService.findById(1L);

        assertThat(result.getTitle()).isEqualTo("Algorithms");
    }

    @Test
    void findById_whenNotFound_throwsException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(99L))
            .isInstanceOf(StudentException.class)
            .hasMessageContaining("99");
    }
}
