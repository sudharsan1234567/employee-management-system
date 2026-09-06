package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.EmployeeStatus;
import com.example.employeemanagement.service.DepartmentService;
import com.example.employeemanagement.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public DashboardController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalEmployees",   employeeService.countAll());
        model.addAttribute("activeEmployees",  employeeService.countByStatus(EmployeeStatus.ACTIVE));
        model.addAttribute("inactiveEmployees",employeeService.countByStatus(EmployeeStatus.INACTIVE));
        model.addAttribute("onLeaveEmployees", employeeService.countByStatus(EmployeeStatus.ON_LEAVE));
        model.addAttribute("totalDepartments", departmentService.getAllDepartments().size());
        model.addAttribute("recentEmployees",  employeeService.getAllEmployees());
        model.addAttribute("departments",      departmentService.getAllDepartments());
        return "dashboard";
    }
}
