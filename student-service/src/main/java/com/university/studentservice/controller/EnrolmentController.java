package com.university.studentservice.controller;

import com.university.studentservice.exception.StudentException;
import com.university.studentservice.model.Student;
import com.university.studentservice.model.User;
import com.university.studentservice.service.EnrolmentService;
import com.university.studentservice.service.StudentService;
import com.university.studentservice.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/enrolments")
public class EnrolmentController {

    private final EnrolmentService enrolmentService;
    private final UserService userService;
    private final StudentService studentService;
    private final String financePortalUrl;

    public EnrolmentController(EnrolmentService enrolmentService,
                               UserService userService,
                               StudentService studentService,
                               @Value("${finance.portal.url}") String financePortalUrl) {
        this.enrolmentService = enrolmentService;
        this.userService = userService;
        this.studentService = studentService;
        this.financePortalUrl = financePortalUrl;
    }

    @PostMapping("/enrol/{courseId}")
    public String enrol(@AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long courseId,
                        Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Student student = studentService.findByUserId(user.getId());
        try {
            enrolmentService.enrol(student, courseId);
            return "redirect:/enrolments?enrolled=true";
        } catch (StudentException e) {
            return "redirect:/courses?error=" + e.getMessage();
        }
    }

    @GetMapping
    public String myEnrolments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Student student = studentService.findByUserId(user.getId());
        model.addAttribute("enrolments", enrolmentService.findByStudent(student));
        model.addAttribute("student", student);
        model.addAttribute("financePortalUrl", financePortalUrl + "/portal");
        return "enrolments";
    }

    @GetMapping("/graduation")
    public String graduation(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Student student = studentService.findByUserId(user.getId());
        model.addAttribute("student", student);
        model.addAttribute("status", enrolmentService.getGraduationStatus(student));
        model.addAttribute("financePortalUrl", financePortalUrl + "/portal");
        return "graduation";
    }
}
