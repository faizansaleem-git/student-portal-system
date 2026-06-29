package com.university.studentservice.repository;

import com.university.studentservice.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTitleContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
            String title, String department);
}
