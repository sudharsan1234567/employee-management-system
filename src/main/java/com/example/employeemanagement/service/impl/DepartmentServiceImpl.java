package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.BusinessRuleException;
import com.example.employeemanagement.exception.DuplicateResourceException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new DuplicateResourceException("Department with name '" + department.getName() + "' already exists.");
        }
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department existingDepartment = getDepartmentById(id);

        if (!existingDepartment.getName().equals(departmentDetails.getName()) &&
                departmentRepository.existsByName(departmentDetails.getName())) {
            throw new DuplicateResourceException("Department with name '" + departmentDetails.getName() + "' already exists.");
        }

        existingDepartment.setName(departmentDetails.getName());
        existingDepartment.setDescription(departmentDetails.getDescription());

        return departmentRepository.save(existingDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = getDepartmentById(id);
        
        List<Employee> employees = employeeRepository.findByDepartment(department);
        if (employees != null && !employees.isEmpty()) {
            throw new BusinessRuleException("Cannot delete department because it still has assigned employees.");
        }
        
        departmentRepository.delete(department);
    }

    @Override
    @Transactional(readOnly = true)
    public Department findDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with name: " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesInDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }
}
