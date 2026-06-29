package com.university.studentservice.controller;

import com.university.studentservice.dto.ProfileUpdateRequest;
import com.university.studentservice.model.Student;
import com.university.studentservice.model.User;
import com.university.studentservice.service.StudentService;
import com.university.studentservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class StudentController {

    private final UserService userService;
    private final StudentService studentService;

    public StudentController(UserService userService, StudentService studentService) {
        this.userService = userService;
        this.studentService = studentService;
    }

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Student student = studentService.findByUserId(user.getId());
        model.addAttribute("student", student);
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Student student = studentService.findByUserId(user.getId());

        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setFirstName(student.getFirstName());
        req.setLastName(student.getLastName());
        req.setEmail(student.getEmail());
        req.setDateOfBirth(student.getDateOfBirth());
        req.setProgramme(student.getProgramme());

        model.addAttribute("student", student);
        model.addAttribute("profileUpdateRequest", req);
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("student", studentService.findByUserId(user.getId()));
            return "profile-edit";
        }
        User user = userService.findByUsername(userDetails.getUsername());
        studentService.updateProfile(user.getId(), request);
        return "redirect:/profile?updated=true";
    }
}
