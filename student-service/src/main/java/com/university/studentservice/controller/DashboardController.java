package com.university.studentservice.controller;

import com.university.studentservice.client.FinanceClient;
import com.university.studentservice.client.LibraryClient;
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

@Controller
public class DashboardController {

    private final UserService userService;
    private final StudentService studentService;
    private final EnrolmentService enrolmentService;
    private final FinanceClient financeClient;
    private final LibraryClient libraryClient;
    private final String financePortalUrl;
    private final String libraryPortalUrl;

    public DashboardController(UserService userService,
                               StudentService studentService,
                               EnrolmentService enrolmentService,
                               FinanceClient financeClient,
                               LibraryClient libraryClient,
                               @Value("${finance.portal.url}") String financePortalUrl,
                               @Value("${library.portal.url}") String libraryPortalUrl) {
        this.userService = userService;
        this.studentService = studentService;
        this.enrolmentService = enrolmentService;
        this.financeClient = financeClient;
        this.libraryClient = libraryClient;
        this.financePortalUrl = financePortalUrl;
        this.libraryPortalUrl = libraryPortalUrl;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Student student = studentService.findByUserId(user.getId());

        // Idempotent — ensures Finance and Library accounts always exist,
        // even if provisioning failed at registration time (e.g. services were down).
        financeClient.createAccount(student.getStudentId());
        libraryClient.createAccount(student.getStudentId(), "000000");

        model.addAttribute("student", student);
        model.addAttribute("activeEnrolments", enrolmentService.countActiveEnrolments(student));
        model.addAttribute("financePortalUrl", financePortalUrl + "/portal");
        model.addAttribute("libraryPortalUrl", libraryPortalUrl + "/dashboard");
        return "dashboard";
    }
}
