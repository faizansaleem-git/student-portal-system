package com.university.studentservice.controller;

import com.university.studentservice.model.Student;
import com.university.studentservice.model.User;
import com.university.studentservice.repository.EnrolmentRepository;
import com.university.studentservice.service.CourseService;
import com.university.studentservice.service.StudentService;
import com.university.studentservice.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserService userService;
    private final StudentService studentService;
    private final EnrolmentRepository enrolmentRepository;

    public CourseController(CourseService courseService,
                            UserService userService,
                            StudentService studentService,
                            EnrolmentRepository enrolmentRepository) {
        this.courseService = courseService;
        this.userService = userService;
        this.studentService = studentService;
        this.enrolmentRepository = enrolmentRepository;
    }

    @GetMapping
    public String listCourses(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) String search,
                              Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Student student = studentService.findByUserId(user.getId());

        Set<Long> enrolledCourseIds = enrolmentRepository.findByStudentId(student.getId())
            .stream()
            .map(e -> e.getCourse().getId())
            .collect(Collectors.toSet());

        model.addAttribute("courses", courseService.search(search));
        model.addAttribute("search", search);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        model.addAttribute("student", student);
        return "courses";
    }
}
