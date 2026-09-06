package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ChangePasswordForm;
import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeePortalController {

    private final UserRepository userRepository;
    private final AttendanceService attendanceService;
    private final PasswordEncoder passwordEncoder;

    public EmployeePortalController(
            UserRepository userRepository,
            AttendanceService attendanceService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.attendanceService = attendanceService;
        this.passwordEncoder = passwordEncoder;
    }

    private Employee getCurrentEmployee(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("No authenticated user found.");
        }
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
        if (user.getEmployee() == null) {
            throw new BusinessRuleException("No employee record linked to current account.");
        }
        return user.getEmployee();
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Employee employee = getCurrentEmployee(authentication);
        Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(employee);

        model.addAttribute("employee", employee);
        model.addAttribute("todayAttendance", todayAttendance.orElse(null));
        model.addAttribute("recentAttendance", attendanceService.getEmployeeRecentAttendance(employee, 5));
        return "employee/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        Employee employee = getCurrentEmployee(authentication);
        model.addAttribute("employee", employee);
        return "employee/profile";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "employee/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Authentication authentication,
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "employee/change-password";
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            model.addAttribute("errorMessage", "Current password is incorrect.");
            return "employee/change-password";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("errorMessage", "New password and Confirm password do not match.");
            return "employee/change-password";
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Your password has been changed successfully.");
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/attendance")
    public String attendance(Authentication authentication, Model model) {
        Employee employee = getCurrentEmployee(authentication);
        Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(employee);

        model.addAttribute("employee", employee);
        model.addAttribute("todayAttendance", todayAttendance.orElse(null));
        model.addAttribute("attendanceList", attendanceService.getEmployeeAttendanceHistory(employee));
        return "employee/attendance";
    }

    @PostMapping("/attendance/check-in")
    public String checkIn(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee(authentication);
            attendanceService.checkIn(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Checked in successfully!");
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }

    @PostMapping("/attendance/check-out")
    public String checkOut(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee(authentication);
            attendanceService.checkOut(employee);
            redirectAttributes.addFlashAttribute("successMessage", "Checked out successfully!");
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }
}
