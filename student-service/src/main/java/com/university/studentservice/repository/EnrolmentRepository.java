package com.university.studentservice.repository;

import com.university.studentservice.model.Enrolment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrolmentRepository extends JpaRepository<Enrolment, Long> {

    List<Enrolment> findByStudentId(Long studentId);

    Optional<Enrolment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByStudentIdAndStatus(Long studentId, String status);
}
