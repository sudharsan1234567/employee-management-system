package com.example.employeemanagement.controller;

import com.example.employeemanagement.service.LeaveRequestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/leave")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLeaveController {

    private final LeaveRequestService leaveRequestService;

    public AdminLeaveController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping
    public String listLeaves(Model model) {
        model.addAttribute("leaves", leaveRequestService.getAllLeaves());
        return "leave/list";
    }

    @PostMapping("/{id}/approve")
    public String approveLeave(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String adminResponse,
            RedirectAttributes redirectAttributes) {
        try {
            leaveRequestService.approveLeave(id, adminResponse);
            redirectAttributes.addFlashAttribute("successMessage", "Leave request approved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/leave";
    }

    @PostMapping("/{id}/reject")
    public String rejectLeave(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String adminResponse,
            RedirectAttributes redirectAttributes) {
        try {
            leaveRequestService.rejectLeave(id, adminResponse);
            redirectAttributes.addFlashAttribute("successMessage", "Leave request rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/leave";
    }
}
