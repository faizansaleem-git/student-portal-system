package com.university.studentservice.service;

import com.university.studentservice.client.FinanceClient;
import com.university.studentservice.client.LibraryClient;
import com.university.studentservice.dto.RegisterRequest;
import com.university.studentservice.exception.StudentException;
import com.university.studentservice.model.Student;
import com.university.studentservice.model.User;
import com.university.studentservice.repository.StudentRepository;
import com.university.studentservice.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final FinanceClient financeClient;
    private final LibraryClient libraryClient;

    public UserService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       PasswordEncoder passwordEncoder,
                       FinanceClient financeClient,
                       LibraryClient libraryClient) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.financeClient = financeClient;
        this.libraryClient = libraryClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    @Transactional
    public Student register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new StudentException("Username already taken: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("STUDENT");
        userRepository.save(user);

        Student student = new Student();
        student.setUser(user);
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());
        student.setProgramme(request.getProgramme());
        studentRepository.save(student);

        // Provision accounts in Finance and Library services
        String studentId = student.getStudentId();
        financeClient.createAccount(studentId);
        // Library PIN defaults to 000000 — matches convention used in library seed data
        libraryClient.createAccount(studentId, "000000");

        return student;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new StudentException("User not found: " + username));
    }
}
