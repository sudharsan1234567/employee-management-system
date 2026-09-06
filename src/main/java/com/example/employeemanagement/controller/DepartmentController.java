package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.DepartmentForm;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // ─── LIST ─────────────────────────────────────────────────────────────────

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentService.getAllDepartments());
        return "departments/list";
    }

    // ─── NEW FORM ─────────────────────────────────────────────────────────────

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newDepartmentForm(Model model) {
        model.addAttribute("departmentForm", new DepartmentForm());
        return "departments/form";
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createDepartment(
            @Valid @ModelAttribute("departmentForm") DepartmentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "departments/form";
        }

        try {
            departmentService.createDepartment(mapFormToDepartment(form, new Department()));
            redirectAttributes.addFlashAttribute("successMessage", "Department created successfully.");
            return "redirect:/departments";
        } catch (DuplicateResourceException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "departments/form";
        }
    }

    // ─── EDIT FORM ────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editDepartmentForm(@PathVariable Long id, Model model) {
        try {
            Department department = departmentService.getDepartmentById(id);
            model.addAttribute("departmentForm", mapDepartmentToForm(department));
            model.addAttribute("employees", departmentService.getEmployeesInDepartment(id));
            return "departments/form";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateDepartment(
            @PathVariable Long id,
            @Valid @ModelAttribute("departmentForm") DepartmentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            try {
                model.addAttribute("employees", departmentService.getEmployeesInDepartment(id));
            } catch (Exception ignored) { /* not fatal */ }
            return "departments/form";
        }

        try {
            departmentService.updateDepartment(id, mapFormToDepartment(form, new Department()));
            redirectAttributes.addFlashAttribute("successMessage", "Department updated successfully.");
            return "redirect:/departments";
        } catch (DuplicateResourceException | ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "departments/form";
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteDepartment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Department deleted successfully.");
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/departments";
    }

    // ─── MAPPING HELPERS ──────────────────────────────────────────────────────

    private Department mapFormToDepartment(DepartmentForm form, Department dept) {
        dept.setName(form.getName());
        dept.setDescription(form.getDescription());
        return dept;
    }

    private DepartmentForm mapDepartmentToForm(Department department) {
        DepartmentForm form = new DepartmentForm();
        form.setId(department.getId());
        form.setName(department.getName());
        form.setDescription(department.getDescription());
        return form;
    }
}
