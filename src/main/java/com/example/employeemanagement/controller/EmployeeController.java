package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeForm;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.EmployeeStatus;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.service.DepartmentService;
import com.example.employeemanagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    // ─── LIST ────────────────────────────────────────────────────────────────

    @GetMapping
    public String listEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            Model model) {

        List<Employee> employees;
        if (search != null && !search.isBlank()) {
            employees = employeeService.searchEmployees(search, search);
        } else if (departmentId != null) {
            employees = employeeService.filterByDepartment(departmentId);
        } else if (status != null) {
            employees = employeeService.filterByStatus(status);
        } else {
            employees = employeeService.getAllEmployees();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("statuses", EmployeeStatus.values());
        model.addAttribute("search", search);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("selectedStatus", status);
        return "employees/list";
    }

    // ─── NEW FORM ─────────────────────────────────────────────────────────────

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newEmployeeForm(Model model) {
        model.addAttribute("employeeForm", new EmployeeForm());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("statuses", EmployeeStatus.values());
        return "employees/form";
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createEmployee(
            @Valid @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            return "employees/form";
        }

        if (form.isCreateAccount() || (form.getUsername() != null && !form.getUsername().trim().isEmpty())) {
            if (form.getPassword() == null || form.getPassword().trim().length() < 6) {
                model.addAttribute("errorMessage", "Password must be at least 6 characters long.");
                model.addAttribute("departments", departmentService.getAllDepartments());
                model.addAttribute("statuses", EmployeeStatus.values());
                return "employees/form";
            }
            if (!form.getPassword().equals(form.getConfirmPassword())) {
                model.addAttribute("errorMessage", "Password and Confirm Password do not match.");
                model.addAttribute("departments", departmentService.getAllDepartments());
                model.addAttribute("statuses", EmployeeStatus.values());
                return "employees/form";
            }
        }

        try {
            Employee employee = mapFormToEmployee(form, new Employee());
            if (form.isCreateAccount() || (form.getUsername() != null && !form.getUsername().trim().isEmpty())) {
                employeeService.createEmployeeWithLogin(employee, form.getUsername(), form.getPassword());
                redirectAttributes.addFlashAttribute("successMessage", "Employee and login account created successfully.");
            } else {
                employeeService.createEmployee(employee);
                redirectAttributes.addFlashAttribute("successMessage", "Employee created successfully.");
            }
            return "redirect:/employees";
        } catch (DuplicateResourceException | BusinessRuleException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            return "employees/form";
        }
    }

    // ─── DETAILS ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public String viewEmployee(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("employee", employeeService.getEmployeeById(id));
            return "employees/view";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    // ─── EDIT FORM ────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            model.addAttribute("employeeForm", mapEmployeeToForm(employee));
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            return "employees/form";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            return "employees/form";
        }

        try {
            Employee updatedEmployee = mapFormToEmployee(form, new Employee());
            employeeService.updateEmployee(id, updatedEmployee);
            redirectAttributes.addFlashAttribute("successMessage", "Employee updated successfully.");
            return "redirect:/employees";
        } catch (DuplicateResourceException | BusinessRuleException | ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", EmployeeStatus.values());
            return "employees/form";
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(id);
            redirectAttributes.addFlashAttribute("successMessage", "Employee deleted successfully.");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete employee: " + e.getMessage());
        }
        return "redirect:/employees";
    }

    // ─── MAPPING HELPERS ──────────────────────────────────────────────────────

    private Employee mapFormToEmployee(EmployeeForm form, Employee employee) {
        employee.setEmployeeCode(form.getEmployeeCode());
        employee.setFirstName(form.getFirstName());
        employee.setLastName(form.getLastName());
        employee.setEmail(form.getEmail());
        employee.setPhone(form.getPhone());
        employee.setDateOfBirth(form.getDateOfBirth());
        employee.setGender(form.getGender());
        employee.setAddress(form.getAddress());
        employee.setHireDate(form.getHireDate());
        employee.setJobTitle(form.getJobTitle());
        employee.setSalary(form.getSalary());
        employee.setStatus(form.getStatus());

        Department dept = new Department();
        dept.setId(form.getDepartmentId());
        employee.setDepartment(dept);

        return employee;
    }

    private EmployeeForm mapEmployeeToForm(Employee employee) {
        EmployeeForm form = new EmployeeForm();
        form.setId(employee.getId());
        form.setEmployeeCode(employee.getEmployeeCode());
        form.setFirstName(employee.getFirstName());
        form.setLastName(employee.getLastName());
        form.setEmail(employee.getEmail());
        form.setPhone(employee.getPhone());
        form.setDateOfBirth(employee.getDateOfBirth());
        form.setGender(employee.getGender());
        form.setAddress(employee.getAddress());
        form.setHireDate(employee.getHireDate());
        form.setJobTitle(employee.getJobTitle());
        form.setSalary(employee.getSalary());
        form.setStatus(employee.getStatus());
        if (employee.getDepartment() != null) {
            form.setDepartmentId(employee.getDepartment().getId());
        }
        return form;
    }
}
