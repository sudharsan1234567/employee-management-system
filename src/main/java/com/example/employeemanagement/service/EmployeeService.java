package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.EmployeeStatus;
import java.util.List;

public interface EmployeeService {
    List<Employee> getAllEmployees();
    Employee getEmployeeById(Long id);
    Employee createEmployee(Employee employee);
    Employee createEmployeeWithLogin(Employee employee, String username, String rawPassword);
    Employee updateEmployee(Long id, Employee employeeDetails);
    void deleteEmployee(Long id);
    Employee findByEmployeeCode(String employeeCode);
    Employee findByEmail(String email);
    List<Employee> searchEmployees(String firstName, String lastName);
    List<Employee> filterByDepartment(Long departmentId);
    List<Employee> filterByStatus(EmployeeStatus status);
    long countByStatus(EmployeeStatus status);
    long countAll();
}
