package com.university.studentservice.service;

import com.university.studentservice.exception.StudentException;
import com.university.studentservice.model.Course;
import com.university.studentservice.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public List<Course> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return courseRepository.findByTitleContainingIgnoreCaseOrDepartmentContainingIgnoreCase(query, query);
    }

    public Course findById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new StudentException("Course not found: " + id));
    }
}
