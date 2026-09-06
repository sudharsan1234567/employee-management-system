package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Department getDepartmentById(Long id);
    Department createDepartment(Department department);
    Department updateDepartment(Long id, Department departmentDetails);
    void deleteDepartment(Long id);
    Department findDepartmentByName(String name);
    List<Employee> getEmployeesInDepartment(Long departmentId);
}
