package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.Attendance;
import com.example.employeemanagement.entity.AttendanceStatus;
import com.example.employeemanagement.service.AttendanceService;
import com.example.employeemanagement.service.DepartmentService;
import com.example.employeemanagement.service.EmployeeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/attendance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public AdminAttendanceController(
            AttendanceService attendanceService,
            EmployeeService employeeService,
            DepartmentService departmentService) {
        this.attendanceService = attendanceService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String listAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) AttendanceStatus status,
            Model model) {

        List<Attendance> attendanceList = attendanceService.getAllAttendance(date, employeeId, departmentId, status);

        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("statuses", AttendanceStatus.values());
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedStatus", status);

        return "attendance/list";
    }

    @GetMapping("/{id}/edit")
    public String editAttendanceForm(@PathVariable Long id, Model model) {
        Attendance attendance = attendanceService.getAttendanceById(id);
        model.addAttribute("attendance", attendance);
        return "attendance/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateAttendance(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut,
            RedirectAttributes redirectAttributes) {
        try {
            attendanceService.updateAttendance(id, checkIn, checkOut);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance record updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/attendance";
    }

    @PostMapping("/{id}/delete")
    public String deleteAttendance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.deleteAttendance(id);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance record deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/attendance";
    }
}
