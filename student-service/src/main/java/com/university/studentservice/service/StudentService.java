package com.university.studentservice.service;

import com.university.studentservice.dto.ProfileUpdateRequest;
import com.university.studentservice.exception.StudentException;
import com.university.studentservice.model.Student;
import com.university.studentservice.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student findByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
            .orElseThrow(() -> new StudentException("Student profile not found for user: " + userId));
    }

    @Transactional
    public Student updateProfile(Long userId, ProfileUpdateRequest request) {
        Student student = findByUserId(userId);
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setProgramme(request.getProgramme());
        return studentRepository.save(student);
    }
}
