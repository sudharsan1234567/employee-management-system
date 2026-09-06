package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.LeaveRequest;
import com.example.employeemanagement.entity.User;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.UserRepository;
import com.example.employeemanagement.service.LeaveRequestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/employee/leave")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeLeaveController {

    private final LeaveRequestService leaveRequestService;
    private final UserRepository userRepository;

    public EmployeeLeaveController(LeaveRequestService leaveRequestService, UserRepository userRepository) {
        this.leaveRequestService = leaveRequestService;
        this.userRepository = userRepository;
    }

    private Employee getCurrentEmployee(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        if (user.getEmployee() == null) {
            throw new BusinessRuleException("No employee profile linked to this account.");
        }
        return user.getEmployee();
    }

    @GetMapping
    public String myLeaves(Authentication auth, Model model) {
        Employee employee = getCurrentEmployee(auth);
        model.addAttribute("leaves", leaveRequestService.getEmployeeLeaves(employee));
        return "employee/leave-list";
    }

    @GetMapping("/apply")
    public String applyLeaveForm(Model model) {
        return "employee/leave-apply";
    }

    @PostMapping("/apply")
    public String applyLeave(
            Authentication auth,
            @RequestParam String leaveType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {
        try {
            Employee employee = getCurrentEmployee(auth);
            leaveRequestService.applyLeave(employee, leaveType, fromDate, toDate, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Leave request submitted successfully.");
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/employee/leave";
    }
}
